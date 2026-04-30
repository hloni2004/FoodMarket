package com.llburgers.repository;

import com.llburgers.domain.BiometricCredential;
import com.llburgers.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BiometricCredentialRepository extends JpaRepository<BiometricCredential, UUID> {
    Optional<BiometricCredential> findByCredentialId(String credentialId);
    List<BiometricCredential> findAllByUser(User user);
}
