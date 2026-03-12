package com.llburgers.repository;

import com.llburgers.domain.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    /** Find the most recent OTP entry for the given email. */
    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);

    /** Remove all OTP entries for an email (cleanup after successful verification). */
    @Modifying
    @Transactional
    void deleteByEmail(String email);
}
