package com.github.UsefulHands.reception.features.guest;

import com.github.UsefulHands.reception.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestService {

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    public List<GuestDto> getAllGuests() {
        log.debug("Fetching all guests from database");

        List<GuestEntity> guests = guestRepository.findAll();
        log.info("Retrieved {} guests", guests.size());

        return guests.stream()
                .map(guestMapper::toDto)
                .toList();
    }

    public GuestDto createGuest(GuestDto guestDto) {
        log.info("Creating a new guest with identification number: {}", guestDto.getIdentificationNumber());

        if (guestRepository.existsByIdentificationNumber(guestDto.getIdentificationNumber())) {
            log.warn("Guest creation failed: Identification number {} already exists", guestDto.getIdentificationNumber());
            throw new ConflictException("Someone else has this ID number!");
        }

        if (guestRepository.existsByEmail(guestDto.getEmail())) {
            log.warn("Guest creation failed: Email {} already exists", guestDto.getEmail());
            throw new ConflictException("This email is already in use!");
        }

        if (guestRepository.existsByPhoneNumber(guestDto.getPhoneNumber())) {
            log.warn("Guest creation failed: Phone number {} already exists", guestDto.getPhoneNumber());
            throw new ConflictException("This phone number is already in use!");
        }

        GuestEntity entity = guestMapper.toEntity(guestDto);
        GuestEntity saved = guestRepository.save(entity);

        log.info("Successfully created guest. Assigned ID: {}", saved.getId());
        return guestMapper.toDto(saved);
    }

}
