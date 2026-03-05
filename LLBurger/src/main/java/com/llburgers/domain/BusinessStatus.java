package com.llburgers.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "lastChangedBy")
public class BusinessStatus {

    /**
     * Singleton row — always ID = 1.
     * Only one record ever exists representing the current live state.
     */
    @Id
    @Column(name = "id")
    @Builder.Default
    private Long id = 1L;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private boolean open = false;

    /**
     * Message shown to customers when the business is closed.
     * Admin can customize this (e.g. "Back at 6pm!", "Closed for the night").
     */
    @Column(name = "closed_message", columnDefinition = "TEXT")
    private String closedMessage;

    /**
     * Optional: admin can set an expected re-open time to display to customers.
     */
    @Column(name = "expected_reopen_at")
    private LocalDateTime expectedReopenAt;

    /**
     * The admin who last toggled the status.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_changed_by")
    private Admin lastChangedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}