package com.github.UsefulHands.reception.features.receptionist.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceptionistRegistrationRequest {
    @Valid
    private ReceptionistDto receptionistDetails;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}