package com.github.UsefulHands.reception.features.reservation;

import java.time.LocalDate;

public record ReservationGridDto(
        Long id,
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status,
        String roomNumber,
        String guestFirstName,
        String guestLastName
) {}
