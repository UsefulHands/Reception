package com.github.UsefulHands.reception.features.guest;

import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.common.exception.UserNotFoundException;
import com.github.UsefulHands.reception.features.user.UserEntity;
import com.github.UsefulHands.reception.features.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GuestDto registerGuest(GuestDto guestDto, String username, String password) {
        log.info("Registering/Updating Guest: {} {}", guestDto.getFirstName(), guestDto.getLastName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_GUEST");

        GuestEntity guestEntity = guestRepository.findByUserId(userEntity.getId())
                .map(existingGuest -> {
                    log.info("Found existing guest profile, updating info...");
                    guestMapper.updateEntityFromDto(guestDto, existingGuest);
                    return existingGuest;
                })
                .orElseGet(() -> {
                    log.info("No existing guest found, creating new profile...");
                    GuestEntity newGuest = guestMapper.toEntity(guestDto);
                    newGuest.setUser(userEntity);
                    return newGuest;
                });

        GuestEntity savedGuest = guestRepository.saveAndFlush(guestEntity);

        GuestDto responseDto = guestMapper.toDto(savedGuest);
        responseDto.setUserId(userEntity.getId());
        return responseDto;
    }

    @Transactional
    public GuestDto editGuest(Long id, GuestDto guestDto) {
        log.info("Editing guest id: {}", id);

        GuestEntity guestEntity = guestRepository.findById(id)
                .filter(g -> g.getUser() == null || !g.getUser().isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Active Guest not found with id: " + id));

        try {
            guestMapper.updateEntityFromDto(guestDto, guestEntity);
            GuestEntity updatedGuest = guestRepository.saveAndFlush(guestEntity);
            return guestMapper.toDto(updatedGuest);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.github.UsefulHands.reception.common.exception.DataIntegrityException(
                    "Identity number or phone number already in use!");
        }
    }

    public List<GuestDto> getGuests() {
        log.info("Retrieving all active guests");
        return guestRepository.findAllActiveGuests()
                .stream()
                .map(guestMapper::toDto)
                .collect(Collectors.toList());
    }

    public GuestDto getGuest(Long id) {
        return guestRepository.findById(id)
                .filter(g -> g.getUser() == null || !g.getUser().isDeleted())
                .map(guestMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Active Guest not found with id: " + id));
    }

    @Transactional
    public GuestDto deleteGuest(Long id) {
        GuestEntity guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));

        if (guest.getUser() != null) {
            guest.getUser().setDeleted(true);
        }

        GuestEntity savedGuest = guestRepository.save(guest);
        log.info("Guest and associated User marked as deleted for Guest ID: {}", id);
        return guestMapper.toDto(savedGuest);
    }

    public GuestEntity findGuestByUserUsername(String username) {
        return guestRepository.findActiveGuestByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Active Guest profile not found for: " + username));
    }

    public GuestDto findByUserId(Long userId) {
        return guestRepository.findByUserId(userId)
                .map(guestMapper::toDto)
                .orElseThrow();
    }

    @Transactional
    public GuestDto updateMyProfile(Long userId, GuestProfileUpdateRequest request) {
        GuestEntity guest = guestRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Guest profile not found for user ID: " + userId));

        if (request.getCurrentPassword() != null && !request.getCurrentPassword().isBlank()) {

            if (!passwordEncoder.matches(request.getCurrentPassword(), guest.getUser().getPassword())) {
                throw new BadCredentialsException("Current password is incorrect!");
            }

            if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
                guest.getUser().setPassword(passwordEncoder.encode(request.getNewPassword()));
            }
        }

        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhoneNumber(request.getPhoneNumber());
        guest.setIdentityNumber(request.getIdentityNumber());

        GuestEntity updatedGuest = guestRepository.save(guest);

        log.info("User {} updated their profile details.", userId);

        return guestMapper.toDto(updatedGuest);
    }

    public GuestDto getMe(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();
        GuestDto guestDto = findByUserId(user.getId());

        if (guestDto == null) {
            throw new UserNotFoundException("Guest record not found");
        }

        return guestDto;
    }

    @Transactional
    public GuestDto updateMe(Authentication authentication, GuestProfileUpdateRequest request) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        GuestEntity guestEntity = guestRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Guest record not found"));

        GuestDto guestDto = GuestDto.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .identityNumber(request.getIdentityNumber())
                .build();

        GuestDto updatedGuest = editGuest(guestEntity.getId(), guestDto);
        if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
            userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        }

        return updatedGuest;
    }


}