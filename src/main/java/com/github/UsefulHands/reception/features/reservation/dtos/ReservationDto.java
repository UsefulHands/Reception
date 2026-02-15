package com.github.UsefulHands.reception.features.reservation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.UsefulHands.reception.features.reservation.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long id;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Guest ID is required")
    private Long guestId;

    private String guestFirstName;
    private String guestLastName;
    private String phoneNumber;
    private String identityNumber;

    // Grid ekranında ID yerine direkt isim görmek için pratik alanlar
    private String guestFullName;
    private String roomNumber;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    private ReservationStatus status;

    private boolean isDeleted = false;

    private BigDecimal totalPrice;
    private BigDecimal balance;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
