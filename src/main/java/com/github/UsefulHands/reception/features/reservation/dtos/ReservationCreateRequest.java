package com.github.UsefulHands.reception.features.reservation.dtos;

import com.github.UsefulHands.reception.features.reservation.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @NotNull  String guestFirstName,
        @NotNull String guestLastName,
        @NotNull String phoneNumber,
        @NotNull String identityNumber,
        ReservationStatus status
) {
    public String firstNameOrEmpty() {
        return guestFirstName != null ? guestFirstName : "";
    }

    public String lastNameOrEmpty() {
        return guestLastName != null ? guestLastName : "";
    }
}