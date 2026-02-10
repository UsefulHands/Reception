package com.github.UsefulHands.reception.features.room;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private String type;

    @NotNull(message = "Room status is required")
    private String status;

    @NotEmpty(message = "At least one bed type must be specified")
    private Set<String> bedTypes;

    @Min(value = 1, message = "Beds count must be at least 1")
    private int beds;

    @Min(value = 1, message = "Maximum guests must be at least 1")
    private int maxGuests;

    private Double areaSqm;

    private String view;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    private boolean available;

    private boolean smokingAllowed;

    @NotNull(message = "Floor information is required")
    private Integer floor;

    private Set<String> amenities;

    private List<String> images;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long currentReservationId;

    private boolean isDeleted = false;
}