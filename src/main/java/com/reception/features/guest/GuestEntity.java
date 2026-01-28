package com.reception.features.guest;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false, length = 11)
    private String identificationNumber;

    private String phoneNumber;

    private String email;

    private LocalDate birthDate;
}
