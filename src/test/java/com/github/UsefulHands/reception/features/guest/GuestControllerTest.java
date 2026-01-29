package com.github.UsefulHands.reception.features.guest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.UsefulHands.reception.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Filters disabled to focus on Controller logic
public class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestService guestService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Scenario 1: Successfully create a new guest.
     * Expected: 201 Created and return the saved GuestDto.
     */
    @Test
    public void createGuest_whenValidInput_shouldReturnCreated() throws Exception {
        // Arrange
        GuestDto guestDto = GuestDto.builder()
                .firstName("Hasan")
                .lastName("Vibe")
                .identificationNumber("12346678901")
                .email("hasan@vibe.com")
                .build();

        when(guestService.createGuest(any(GuestDto.class))).thenReturn(guestDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Hasan"))
                .andExpect(jsonPath("$.email").value("hasan@vibe.com"));
    }

    /**
     * Scenario 2: Retrieve all guests.
     * Expected: 200 OK and a list of GuestDto objects.
     */
    @Test
    public void getAllGuests_shouldReturnGuestList() throws Exception {
        // Arrange
        List<GuestDto> guests = List.of(
                GuestDto.builder().firstName("Hasan").build(),
                GuestDto.builder().firstName("Ali").build()
        );

        when(guestService.getAllGuests()).thenReturn(guests);

        // Act & Assert
        mockMvc.perform(get("/api/v1/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Hasan"));
    }

    /**
     * Scenario 3: Attempt to create a guest with an existing Identification Number.
     * Expected: 409 Conflict.
     */
    @Test
    public void createGuest_whenIdExists_shouldReturnConflict() throws Exception {
        // Arrange: Fill all mandatory fields so validation passes
        GuestDto dto = GuestDto.builder()
                .firstName("Hakan")
                .lastName("Yilmaz") // Added to pass validation
                .identificationNumber("12345678901")
                .email("test@test.com") // Added to pass validation
                .build();

        // Now the validation passes, so the request reaches the Service
        // and the Service throws our custom ConflictException
        when(guestService.createGuest(any(GuestDto.class)))
                .thenThrow(new ConflictException("Someone else has this id number"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // Now it should be 409
    }

    /**
     * Scenario 4: Attempt to create a guest with missing mandatory fields.
     * Expected: 400 Bad Request due to @Valid constraints.
     */
    @Test
    public void createGuest_whenMissingFields_shouldReturnBadRequest() throws Exception {
        // Arrange: Missing firstName and empty identificationNumber
        GuestDto invalidDto = GuestDto.builder()
                .lastName("NoFirstName")
                .identificationNumber("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Scenario 5: Attempt to create a guest with an invalid email format.
     * Expected: 400 Bad Request.
     */
    @Test
    public void createGuest_whenInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        GuestDto invalidDto = GuestDto.builder()
                .firstName("Ali")
                .email("not-an-email-address")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}