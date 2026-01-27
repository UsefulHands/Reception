package com.reception.features.guest;

import com.reception.features.guest.GuestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;
    private final GuestMapper guestMapper;

    @PostMapping
    public GuestDto createGuest(@RequestBody GuestDto guestDto) {
        GuestEntity entity = guestMapper.toEntity(guestDto);
        GuestEntity savedEntity = guestService.createGuest(entity);
        return guestMapper.toDto(savedEntity);
    }

    @GetMapping
    public List<GuestDto> getAllGuests() {
        return guestService.getAllGuests().stream()
                .map(guestMapper::toDto)
                .collect(Collectors.toList());
    }
}
