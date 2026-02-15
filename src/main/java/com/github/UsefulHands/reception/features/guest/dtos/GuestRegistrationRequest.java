package com.github.UsefulHands.reception.features.guest.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestRegistrationRequest {
    @Valid
    private GuestDto guestDto;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}