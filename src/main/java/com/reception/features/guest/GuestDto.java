package com.reception.features.guest;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String identificationNumber;
    private String phoneNumber;
    private String email;
}
