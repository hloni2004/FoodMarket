package com.llburgers.service;

import com.llburgers.domain.BiometricCredential;
import com.llburgers.domain.User;
import com.llburgers.dto.WebAuthnDto;
import com.llburgers.repository.BiometricCredentialRepository;
import com.llburgers.repository.UserRepository;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey;
import com.webauthn4j.data.attestation.authenticator.RSACOSEKey;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebAuthnService {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnService.class);
    private static final int CHALLENGE_BYTES = 32;
    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final BiometricCredentialRepository biometricCredentialRepository;
    private final WebAuthnManager webAuthnManager;
    private final SecureRandom secureRandom;
    private final Map<String, ChallengeRecord> challengeStore = new ConcurrentHashMap<>();
    private final String rpId;
    private final String rpName;
    private final String origin;

    public WebAuthnService(UserRepository userRepository,
                           BiometricCredentialRepository biometricCredentialRepository,
                           @Value("${webauthn.rp-id}") String rpId,
                           @Value("${webauthn.rp-name}") String rpName,
                           @Value("${webauthn.origin}") String origin) {
        this.userRepository = userRepository;
        this.biometricCredentialRepository = biometricCredentialRepository;
        this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
        this.secureRandom = new SecureRandom();
        this.rpId = rpId;
        this.rpName = rpName;
        this.origin = origin;
    }

    public WebAuthnDto.RegistrationOptionsResponse startRegistration(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive.");
        }

        String challengeId = UUID.randomUUID().toString();
        byte[] challenge = new byte[CHALLENGE_BYTES];
        secureRandom.nextBytes(challenge);
        challengeStore.put(challengeId, new ChallengeRecord(user.getId(), challenge, ChallengeType.REGISTRATION, Instant.now()));
        cleanupChallenges();

        WebAuthnDto.PublicKeyCredentialCreationOptions options =
            new WebAuthnDto.PublicKeyCredentialCreationOptions(
                encodeBase64Url(challenge),
                new WebAuthnDto.RelyingParty(rpId, rpName),
                new WebAuthnDto.WebAuthnUser(
                    encodeBase64Url(user.getId().toString().getBytes(StandardCharsets.UTF_8)),
                    user.getEmail(),
                    user.getName()
                ),
                List.of(
                    new WebAuthnDto.PublicKeyCredentialParameters("public-key", -7),
                    new WebAuthnDto.PublicKeyCredentialParameters("public-key", -257)
                ),
                60000L,
                "none",
                "required"
            );

        return new WebAuthnDto.RegistrationOptionsResponse(challengeId, options);
    }

    public void finishRegistration(User user, WebAuthnDto.RegistrationFinishRequest request) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User not found.");
        }

        ChallengeRecord record = consumeChallenge(request.challengeId(), ChallengeType.REGISTRATION, user.getId());

        byte[] attestationObject = decodeBase64Url(request.attestationObject());
        byte[] clientDataJson = decodeBase64Url(request.clientDataJSON());
        Set<String> transports = request.transports() == null ? null : Set.copyOf(request.transports());

        RegistrationRequest registrationRequest = transports == null
            ? new RegistrationRequest(attestationObject, clientDataJson)
            : new RegistrationRequest(attestationObject, clientDataJson, null, transports);

        ServerProperty serverProperty = new ServerProperty(
            Origin.create(origin),
            rpId,
            new DefaultChallenge(record.challenge()),
            null
        );
        RegistrationParameters parameters = new RegistrationParameters(serverProperty, true, true);

        RegistrationData registrationData = webAuthnManager.validate(registrationRequest, parameters);
        AuthenticatorImpl authenticator = AuthenticatorImpl.createFromRegistrationData(registrationData);
        AttestedCredentialData credentialData = authenticator.getAttestedCredentialData();

        String credentialId = encodeBase64Url(credentialData.getCredentialId());
        if (request.credentialId() != null && !request.credentialId().isBlank()
                && !credentialId.equals(request.credentialId())) {
            throw new IllegalArgumentException("Credential ID mismatch.");
        }
        if (biometricCredentialRepository.findByCredentialId(credentialId).isPresent()) {
            throw new IllegalArgumentException("Credential already registered.");
        }

        String publicKey = Base64.getEncoder().encodeToString(
            credentialData.getCOSEKey().getPublicKey().getEncoded()
        );
        long signCount = registrationData.getAttestationObject()
            .getAuthenticatorData()
            .getSignCount();

        BiometricCredential credential = BiometricCredential.builder()
            .user(user)
            .credentialId(credentialId)
            .publicKey(publicKey)
            .signatureCount(signCount)
            .build();
        biometricCredentialRepository.save(credential);

        log.info("[WEBAUTHN-REGISTER] userId={}, credentialId={}", user.getId(), credentialId);
    }

    public WebAuthnDto.AuthenticationOptionsResponse startAuthentication(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or credentials."));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Invalid email or credentials.");
        }

        List<BiometricCredential> credentials = biometricCredentialRepository.findAllByUser(user);
        if (credentials.isEmpty()) {
            throw new IllegalArgumentException("No biometric credentials registered.");
        }

        String challengeId = UUID.randomUUID().toString();
        byte[] challenge = new byte[CHALLENGE_BYTES];
        secureRandom.nextBytes(challenge);
        challengeStore.put(challengeId, new ChallengeRecord(user.getId(), challenge, ChallengeType.AUTHENTICATION, Instant.now()));
        cleanupChallenges();

        List<WebAuthnDto.AllowedCredential> allowCredentials = credentials.stream()
            .map(credential -> new WebAuthnDto.AllowedCredential("public-key", credential.getCredentialId()))
            .toList();

        WebAuthnDto.PublicKeyCredentialRequestOptions options =
            new WebAuthnDto.PublicKeyCredentialRequestOptions(
                encodeBase64Url(challenge),
                60000L,
                rpId,
                "required",
                allowCredentials
            );

        return new WebAuthnDto.AuthenticationOptionsResponse(challengeId, options);
    }

    public User finishAuthentication(WebAuthnDto.AuthenticationFinishRequest request) {
        if (request.credentialId() == null || request.credentialId().isBlank()) {
            throw new IllegalArgumentException("Credential ID is required.");
        }

        ChallengeRecord record = consumeChallenge(request.challengeId(), ChallengeType.AUTHENTICATION, null);

        BiometricCredential credential = biometricCredentialRepository
            .findByCredentialId(request.credentialId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credential."));

        if (record.userId() != null && !record.userId().equals(credential.getUser().getId())) {
            throw new IllegalArgumentException("Credential does not match this login session.");
        }

        byte[] credentialId = decodeBase64Url(request.credentialId());
        byte[] authenticatorData = decodeBase64Url(request.authenticatorData());
        byte[] clientDataJson = decodeBase64Url(request.clientDataJSON());
        byte[] signature = decodeBase64Url(request.signature());
        byte[] userHandle = request.userHandle() == null || request.userHandle().isBlank()
            ? null
            : decodeBase64Url(request.userHandle());

        PublicKey publicKey = decodePublicKey(Base64.getDecoder().decode(credential.getPublicKey()));
        COSEKey coseKey = toCoseKey(publicKey);
        AttestedCredentialData attestedCredentialData = new AttestedCredentialData(
            AAGUID.ZERO,
            credentialId,
            coseKey
        );
        AuthenticatorImpl authenticator = new AuthenticatorImpl(
            attestedCredentialData,
            null,
            credential.getSignatureCount()
        );

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
            credentialId,
            userHandle,
            authenticatorData,
            clientDataJson,
            null,
            signature
        );

        ServerProperty serverProperty = new ServerProperty(
            Origin.create(origin),
            rpId,
            new DefaultChallenge(record.challenge()),
            null
        );
        AuthenticationParameters parameters = new AuthenticationParameters(serverProperty, authenticator, true, true);

        AuthenticationData authenticationData = webAuthnManager.validate(authenticationRequest, parameters);
        long newCounter = authenticationData.getAuthenticatorData().getSignCount();
        if (newCounter > credential.getSignatureCount()) {
            credential.setSignatureCount(newCounter);
            biometricCredentialRepository.save(credential);
        }

        log.info("[WEBAUTHN-LOGIN] userId={}, credentialId={}", credential.getUser().getId(), credential.getCredentialId());
        return credential.getUser();
    }

    private ChallengeRecord consumeChallenge(String challengeId, ChallengeType expectedType, UUID expectedUserId) {
        if (challengeId == null || challengeId.isBlank()) {
            throw new IllegalArgumentException("Challenge ID is required.");
        }
        ChallengeRecord record = challengeStore.remove(challengeId);
        if (record == null) {
            throw new IllegalArgumentException("Challenge expired or invalid.");
        }
        if (record.type() != expectedType) {
            throw new IllegalArgumentException("Challenge type mismatch.");
        }
        if (expectedUserId != null && !expectedUserId.equals(record.userId())) {
            throw new IllegalArgumentException("Challenge does not belong to this user.");
        }
        if (Instant.now().isAfter(record.createdAt().plus(CHALLENGE_TTL))) {
            throw new IllegalArgumentException("Challenge expired.");
        }
        return record;
    }

    private void cleanupChallenges() {
        Instant now = Instant.now();
        challengeStore.entrySet().removeIf(entry ->
            now.isAfter(entry.getValue().createdAt().plus(CHALLENGE_TTL))
        );
    }

    private String encodeBase64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decodeBase64Url(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required WebAuthn payload.");
        }
        return Base64.getUrlDecoder().decode(value);
    }

    private PublicKey decodePublicKey(byte[] encoded) {
        try {
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception ecError) {
            try {
                return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
            } catch (Exception rsaError) {
                log.warn("[WEBAUTHN] Unable to decode public key: {}", rsaError.getMessage());
                throw new IllegalArgumentException("Unsupported credential key type.");
            }
        }
    }

    private COSEKey toCoseKey(PublicKey publicKey) {
        if (publicKey instanceof ECPublicKey ecPublicKey) {
            return EC2COSEKey.create(ecPublicKey);
        }
        if (publicKey instanceof RSAPublicKey rsaPublicKey) {
            return RSACOSEKey.create(rsaPublicKey);
        }
        throw new IllegalArgumentException("Unsupported credential key type.");
    }

    private enum ChallengeType {
        REGISTRATION,
        AUTHENTICATION
    }

    private record ChallengeRecord(UUID userId, byte[] challenge, ChallengeType type, Instant createdAt) {
    }
}
