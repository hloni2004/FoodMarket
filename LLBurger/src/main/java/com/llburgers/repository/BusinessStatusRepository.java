package com.llburgers.repository;

import com.llburgers.domain.BusinessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessStatusRepository extends JpaRepository<BusinessStatus, Long> {

    // ─── Singleton Access ─────────────────────────────────────────────────────

    /**
     * Retrieves the single live business status record (ID = 1).
     * Use this instead of findById(1L) for readability.
     */
    default Optional<BusinessStatus> findCurrent() {
        return findById(1L);
    }

    // ─── By State ─────────────────────────────────────────────────────────────

    Optional<BusinessStatus> findByOpen(boolean open);
}

