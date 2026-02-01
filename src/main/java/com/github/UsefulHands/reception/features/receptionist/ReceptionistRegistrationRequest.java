package com.github.UsefulHands.reception.features.receptionist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceptionistRegistrationRequest {
    @Valid
    private ReceptionistDto receptionistDto;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}