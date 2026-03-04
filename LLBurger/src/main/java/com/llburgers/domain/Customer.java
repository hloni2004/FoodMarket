package com.llburgers.domain;

import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends User {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Block block;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    /**
     * Payment methods stored as JSON string.
     * Consider a dedicated PaymentMethod entity for full normalization.
     */
    @Column(name = "payment_methods", columnDefinition = "TEXT")
    private String paymentMethods;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void enforceRole() {
        setRole(Role.CUSTOMER);
    }
}
