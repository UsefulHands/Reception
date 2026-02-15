package com.github.UsefulHands.reception.features.reservation.dtos;

import java.time.LocalDate;

public record ReservationNoInfoGridDto(
        Long id,
        Long roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status,
        String roomNumber
) {}
