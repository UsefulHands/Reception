package com.github.UsefulHands.reception.features.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody UserDto userDto) {
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody UserDto userDto) {
        return userService.login(userDto);
    }
}
