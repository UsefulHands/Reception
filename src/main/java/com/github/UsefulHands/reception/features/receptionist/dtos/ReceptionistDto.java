package com.github.UsefulHands.reception.features.receptionist.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String shiftType; // Morning, Evening, Night

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;
}