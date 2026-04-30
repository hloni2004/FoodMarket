package com.llburgers.dto;

import java.util.List;

public final class WebAuthnDto {

    private WebAuthnDto() {
    }

    public record RegistrationOptionsResponse(
        String challengeId,
        PublicKeyCredentialCreationOptions publicKey
    ) {
    }

    public record AuthenticationOptionsResponse(
        String challengeId,
        PublicKeyCredentialRequestOptions publicKey
    ) {
    }

    public record AuthenticationOptionsRequest(String email) {
    }

    public record RegistrationFinishRequest(
        String challengeId,
        String credentialId,
        String attestationObject,
        String clientDataJSON,
        List<String> transports
    ) {
    }

    public record AuthenticationFinishRequest(
        String challengeId,
        String credentialId,
        String authenticatorData,
        String clientDataJSON,
        String signature,
        String userHandle
    ) {
    }

    public record PublicKeyCredentialCreationOptions(
        String challenge,
        RelyingParty rp,
        WebAuthnUser user,
        List<PublicKeyCredentialParameters> pubKeyCredParams,
        Long timeout,
        String attestation,
        String userVerification
    ) {
    }

    public record PublicKeyCredentialRequestOptions(
        String challenge,
        Long timeout,
        String rpId,
        String userVerification,
        List<AllowedCredential> allowCredentials
    ) {
    }

    public record RelyingParty(String id, String name) {
    }

    public record WebAuthnUser(String id, String name, String displayName) {
    }

    public record PublicKeyCredentialParameters(String type, int alg) {
    }

    public record AllowedCredential(String type, String id) {
    }
}
