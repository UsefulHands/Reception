package com.github.UsefulHands.reception.features.guest.dtos;

import lombok.Data;

@Data
public class GuestProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String identityNumber;

    private String currentPassword;
    private String newPassword;
}
