package com.github.UsefulHands.reception.features.guest;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.features.admin.AdminDto;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestService {

    private final GuestRepository guestRepository;
    private final UserService userService;
    private final GuestMapper guestMapper;

    @Transactional
    public GuestDto registerGuest(GuestDto guestDto, String username, String password) {
        log.info("Registering Guest: {} {}", guestDto.getFirstName(), guestDto.getLastName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_GUEST");

        GuestEntity guestEntity = guestRepository.findByUserId(userEntity.getId())
                .map(existingGuest -> {
                    log.info("Found existing guest profile for user, updating info...");
                    guestMapper.updateEntityFromDto(guestDto, existingGuest);
                    return existingGuest;
                })
                .orElseGet(() -> {
                    log.info("No existing guest found, creating new profile...");
                    GuestEntity newGuest = guestMapper.toEntity(guestDto);
                    newGuest.setUser(userEntity);
                    return newGuest;
                });

        GuestEntity savedGuest = guestRepository.save(guestEntity);

        GuestDto responseDto = guestMapper.toDto(savedGuest);
        responseDto.setUserId(userEntity.getId());
        return responseDto;
    }

    @Transactional
    public GuestDto editGuest(Long id, GuestDto guestDto) {
        log.info("Editing guest");

        GuestEntity guestEntity = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));

        guestMapper.updateEntityFromDto(guestDto, guestEntity);

        GuestEntity updatedGuest = guestRepository.save(guestEntity);
        return guestMapper.toDto(updatedGuest);
    }

    public List<GuestDto> getAllGuests() {
        log.info("Retrieving guests");
        return guestRepository.findAllActiveGuests()
                .stream()
                .map(guestMapper::toDto)
                .collect(Collectors.toList());
    }

    public GuestDto getGuest(Long id){
        log.info("Retrieving guest");
        return guestRepository.findById(id)
                .map(guestMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
    }

    @Transactional
    public GuestDto deleteGuest(Long guestId) {
        GuestEntity guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));

        if (guest.getUser() != null) {
            guest.getUser().setDeleted(true);
        }

        guestRepository.save(guest);

        log.info("Guest and associated User marked as deleted for Guest ID: {}", guestId);
        return guestMapper.toDto(guest);
    }
}