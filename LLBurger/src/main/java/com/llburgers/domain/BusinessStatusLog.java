package com.llburgers.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log — every time an admin toggles the business open/closed,
 * a new record is written here. Useful for analytics (peak hours, total open time)
 * and accountability (which admin closed the business and when).
 */
@Entity
@Table(name = "business_status_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "changedBy")
public class BusinessStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The new state that was applied (true = opened, false = closed).
     */
    @Column(name = "is_open", nullable = false)
    private boolean open;

    /**
     * Closed message set at the time of this toggle (nullable if business was opened).
     */
    @Column(name = "closed_message", columnDefinition = "TEXT")
    private String closedMessage;

    @Column(name = "expected_reopen_at")
    private LocalDateTime expectedReopenAt;

    /**
     * The admin who performed the toggle.
     * Nullable to preserve audit log if admin is deleted.
     */
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Admin changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}