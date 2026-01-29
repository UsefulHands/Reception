package com.github.UsefulHands.reception.features.guest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestDto createGuest(@Valid @RequestBody GuestDto guestDto) {
        return guestService.createGuest(guestDto);
    }

    @GetMapping
    public List<GuestDto> getAllGuests() {
        return guestService.getAllGuests();
    }
}
