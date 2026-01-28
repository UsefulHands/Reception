package com.reception.features.guest;

import com.reception.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito support
public class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private GuestMapper guestMapper;

    @InjectMocks
    private GuestService guestService; // Injects the mocks above into this service

    /**
     * Scenario: Successfully create a guest.
     * Logic: No conflicts found, repository saves, and mapper returns DTO.
     */
    @Test
    void createGuest_whenAllValid_shouldSaveAndReturnDto() {
        // Arrange
        GuestDto inputDto = GuestDto.builder()
                .identificationNumber("11111111111")
                .email("test@mail.com")
                .phoneNumber("5554443322")
                .build();

        GuestEntity entity = new GuestEntity();
        GuestEntity savedEntity = new GuestEntity();
        savedEntity.setId(1L);

        // Define behavior for mocks
        when(guestRepository.existsByIdentificationNumber(anyString())).thenReturn(false);
        when(guestRepository.existsByEmail(anyString())).thenReturn(false);
        when(guestRepository.existsByPhoneNumber(anyString())).thenReturn(false);

        when(guestMapper.toEntity(any(GuestDto.class))).thenReturn(entity);
        when(guestRepository.save(any(GuestEntity.class))).thenReturn(savedEntity);
        when(guestMapper.toDto(any(GuestEntity.class))).thenReturn(inputDto);

        // Act
        GuestDto result = guestService.createGuest(inputDto);

        // Assert
        assertNotNull(result);
        verify(guestRepository, times(1)).save(any(GuestEntity.class)); // Check if save was called
        verify(guestMapper, times(1)).toEntity(any(GuestDto.class));
    }

    /**
     * Scenario: Attempt to create a guest with a duplicate ID.
     * Expected: ConflictException must be thrown.
     */
    @Test
    void createGuest_whenIdExists_shouldThrowConflictException() {
        // Arrange
        GuestDto dto = GuestDto.builder().identificationNumber("11111111111").build();
        when(guestRepository.existsByIdentificationNumber("11111111111")).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            guestService.createGuest(dto);
        });

        assertEquals("Someone else has this ID number!", exception.getMessage());
        verify(guestRepository, never()).save(any()); // Important: Should NOT save if conflict exists
    }

    /**
     * Scenario: Attempt to create a guest with an existing email.
     * Expected: ConflictException.
     */
    @Test
    void createGuest_whenEmailExists_shouldThrowConflictException() {
        // Arrange
        GuestDto dto = GuestDto.builder()
                .identificationNumber("11111111111")
                .email("exists@mail.com")
                .build();

        when(guestRepository.existsByIdentificationNumber(anyString())).thenReturn(false);
        when(guestRepository.existsByEmail("exists@mail.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> guestService.createGuest(dto));
        verify(guestRepository, never()).save(any());
    }

    /**
     * Scenario: Attempt to create a guest with an existing phone number.
     * Expected: ConflictException.
     */
    @Test
    void createGuest_whenPhoneNumberExists_shouldThrowConflictException() {
        // Arrange
        GuestDto dto = GuestDto.builder()
                .identificationNumber("11111111111")
                .email("new@mail.com")
                .phoneNumber("5554443322")
                .build();

        when(guestRepository.existsByIdentificationNumber(anyString())).thenReturn(false);
        when(guestRepository.existsByEmail(anyString())).thenReturn(false);
        when(guestRepository.existsByPhoneNumber("5554443322")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> guestService.createGuest(dto));
        verify(guestRepository, never()).save(any());
    }

    /**
     * Scenario: Retrieve all guests.
     * Expected: A list of GuestDtos.
     */
    @Test
    void getAllGuests_shouldReturnDtoList() {
        // Arrange
        GuestEntity entity = new GuestEntity();
        when(guestRepository.findAll()).thenReturn(List.of(entity));
        when(guestMapper.toDto(any(GuestEntity.class))).thenReturn(GuestDto.builder().build());

        // Act
        List<GuestDto> results = guestService.getAllGuests();

        // Assert
        assertEquals(1, results.size());
        verify(guestRepository, times(1)).findAll();
    }
}