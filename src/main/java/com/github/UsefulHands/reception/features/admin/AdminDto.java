package com.github.UsefulHands.reception.features.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Please provide a valid corporate email")
    @NotBlank(message = "Corporate email is required")
    private String corporateEmail;

    private String adminTitle;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;
}