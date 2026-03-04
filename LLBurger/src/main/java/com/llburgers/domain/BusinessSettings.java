package com.llburgers.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isOpen;

    private String welcomeMessage;
}