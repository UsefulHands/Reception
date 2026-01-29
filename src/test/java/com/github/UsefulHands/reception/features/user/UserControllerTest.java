package com.github.UsefulHands.reception.features.user;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for controller unit testing
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Scenario: Successfully register a new user.
     * Expected: 200 OK and return the UserDto (without password).
     */
    @Test
    void register_whenValidInput_shouldReturnUserDto() throws Exception {
        // Arrange: Using a raw JSON string to ensure the password is sent in the request
        String registerJson = """
        {
            "username": "hasan_vibe",
            "password": "secret123"
        }
        """;

        UserDto savedDto = UserDto.builder()
                .id(1L)
                .username("hasan_vibe")
                .build();

        when(userService.register(any(UserDto.class))).thenReturn(savedDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson)) // Send the raw JSON including password
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("hasan_vibe"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist()); // Verify password is NOT in response
    }

    /**
     * Scenario: Register with a weak password (less than 3 chars).
     * Expected: 400 Bad Request due to @Size(min = 3) in UserDto.
     */
    @Test
    void register_whenPasswordTooShort_shouldReturnBadRequest() throws Exception {
        // Arrange
        UserDto invalidDto = UserDto.builder()
                .username("hasan")
                .password("12") // Too short
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Scenario: Successfully login.
     * Expected: 200 OK and return a JWT String.
     */
    @Test
    void login_whenValidCredentials_shouldReturnJwtToken() throws Exception {
        // Arrange: Use a manual JSON string because ObjectMapper skips WRITE_ONLY fields (password)
        String loginJson = """
        {
            "username": "hasan_vibe",
            "password": "secret123"
        }
        """;

        String fakeToken = "mocked-jwt-token";

        // mock the service to return our fake token
        when(userService.login(any(UserDto.class))).thenReturn(fakeToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson)) // Send the raw JSON string
                .andExpect(status().isOk())
                .andExpect(content().string(fakeToken));
    }

    /**
     * Scenario: Register with an already existing username.
     * Expected: 409 Conflict or custom exception status.
     */
    @Test
    void register_whenUsernameExists_shouldReturnConflict() throws Exception {
        // Arrange
        String userJson = """
        {
            "username": "existing_user",
            "password": "password123"
        }
        """;

        when(userService.register(any(UserDto.class)))
                .thenThrow(new ConflictException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)) // ObjectMapper yerine direkt string veriyoruz
                .andExpect(status().isConflict());
    }
}
