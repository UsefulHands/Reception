package com.github.UsefulHands.reception.features.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        String firstName,
        String lastName,
        String phoneNumber,
        String identityNumber,
        ReservationStatus status
) {}