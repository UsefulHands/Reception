package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.features.guest.GuestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegistrationRequest {
    @Valid
    private AdminDto adminDto;

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
