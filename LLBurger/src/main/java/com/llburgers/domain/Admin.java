package com.llburgers.domain;

import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Admin extends User {

    /**
     * Admins can have a specific permission level for future
     * granular access control (e.g. SUPER_ADMIN vs regular ADMIN).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level", nullable = false)
    @Builder.Default
    private AdminLevel adminLevel = AdminLevel.ADMIN;

    @PrePersist
    @PreUpdate
    private void enforceRole() {
        setRole(Role.ADMIN);
    }
}