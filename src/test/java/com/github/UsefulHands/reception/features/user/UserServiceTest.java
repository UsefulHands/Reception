package com.github.UsefulHands.reception.features.user;

import com.github.UsefulHands.reception.common.exception.UserNotFoundException;
import com.github.UsefulHands.reception.common.exception.UsernameAlreadyExistsException;
import com.github.UsefulHands.reception.common.exception.WrongPasswordException;
import com.github.UsefulHands.reception.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    // --- REGISTER TESTS ---

    /**
     * Scenario 1: Successful Registration
     */
    @Test
    void register_whenUsernameNotExists_shouldSaveUser() {
        // Arrange
        UserDto inputDto = UserDto.builder().username("hasan").password("pass123").build();
        UserEntity savedUser = UserEntity.builder().id(1L).username("hasan").build();

        when(userRepository.existsByUsernameIgnoreCase("hasan")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UserDto result = userService.register(inputDto);

        // Assert
        assertNotNull(result.getId());
        assertEquals("hasan", result.getUsername());
        verify(userRepository).save(any(UserEntity.class));
    }

    /**
     * Scenario 2: Registration Fail - Username Already Exists
     */
    @Test
    void register_whenUsernameExists_shouldThrowException() {
        // Arrange
        UserDto inputDto = UserDto.builder().username("hasan").build();
        when(userRepository.existsByUsernameIgnoreCase("hasan")).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(inputDto));
        verify(userRepository, never()).save(any());
    }

    // --- LOGIN TESTS ---

    /**
     * Scenario 3: Successful Login
     * Expected: Passwords match and a JWT token is returned.
     */
    @Test
    void login_whenCredentialsAreValid_shouldReturnToken() {
        // Arrange
        UserDto loginDto = UserDto.builder().username("hasan").password("pass123").build();
        UserEntity userEntity = UserEntity.builder().username("hasan").password("hashed_pass").build();

        when(userRepository.findByUsernameIgnoreCase("hasan")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("pass123", "hashed_pass")).thenReturn(true);
        when(jwtService.generateToken("hasan")).thenReturn("mocked_jwt_token");

        // Act
        String token = userService.login(loginDto);

        // Assert
        assertEquals("mocked_jwt_token", token);
    }

    /**
     * Scenario 4: Login Fail - User Not Found or Wrong Password
     */
    @Test
    void login_whenUserNotFound_shouldThrowException() {
        // Arrange
        UserDto loginDto = UserDto.builder().username("unknown").build();
        when(userRepository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.login(loginDto));
    }

    @Test
    void login_whenPasswordWrong_shouldThrowException() {
        // Arrange
        UserDto loginDto = UserDto.builder().username("hasan").password("wrong").build();
        UserEntity userEntity = UserEntity.builder().username("hasan").password("hashed_pass").build();

        when(userRepository.findByUsernameIgnoreCase("hasan")).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrong", "hashed_pass")).thenReturn(false);

        // Act & Assert
        assertThrows(WrongPasswordException.class, () -> userService.login(loginDto));
    }
}
