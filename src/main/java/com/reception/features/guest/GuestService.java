package com.reception.features.guest;

import com.reception.common.exception.ConflictException;
import com.reception.features.user.UserEntity;
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
        return guestRepository.findAll().stream()
                .map(guestMapper::toDto)
                .toList();
    }

    public GuestDto createGuest(GuestDto guestDto) {
        if (guestRepository.existsByIdentificationNumber(guestDto.getIdentificationNumber())) {
            throw new ConflictException("Someone else has this ID number!");
        }

        if (guestRepository.existsByEmail(guestDto.getEmail())) {
            throw new ConflictException("This email is already in use!");
        }

        if (guestRepository.existsByPhoneNumber(guestDto.getPhoneNumber())) {
            throw new ConflictException("This phone number is already in use!");
        }

        GuestEntity entity = guestMapper.toEntity(guestDto);

        GuestEntity saved = guestRepository.save(entity);
        log.info("Guest created with ID: {}", saved.getId());

        return guestMapper.toDto(saved);
    }
}
