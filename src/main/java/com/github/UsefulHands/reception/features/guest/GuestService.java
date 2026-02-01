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
        log.info("Registering new Guest: {}", guestDto.getFirstName());

        UserEntity userEntity = userService.createAccount(username, password, "ROLE_GUEST");
        GuestEntity guestEntity = guestMapper.toEntity(guestDto);
        guestEntity.setUser(userEntity);
        GuestEntity savedGuest = guestRepository.save(guestEntity);
        log.info("Guest profile saved with ID: {} for User ID: {}", savedGuest.getId(), userEntity.getId());

        guestDto.setId(savedGuest.getId());
        guestDto.setUserId(userEntity.getId());
        return guestMapper.toDto(savedGuest);
    }

    public List<GuestDto> getAllGuests() {
        log.info("Retrieving guests");
        return guestRepository.findAll()
                .stream()
                .map(guestMapper::toDto)
                .collect(Collectors.toList());
    }

    public GuestDto getGuest(Long id){
        log.info("Retrieving guest");
        return guestRepository.findByUserId(id)
                .map(guestMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
    }
}