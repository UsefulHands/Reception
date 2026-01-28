package com.reception.features.user;

import com.reception.common.exception.UserNotFoundException;
import com.reception.common.exception.UsernameAlreadyExistsException;
import com.reception.common.exception.WrongPasswordException;
import com.reception.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 1. Loglama için eklendi
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserDto register(UserDto userDto) {
        log.info("Registration attempt for username: {}", userDto.getUsername());

        if (userRepository.existsByUsernameIgnoreCase(userDto.getUsername())) {
            log.warn("Registration failed: Username '{}' is already taken", userDto.getUsername());
            throw new UsernameAlreadyExistsException("This username has been taken!");
        }

        UserEntity user = UserEntity.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("User successfully registered with ID: {}", savedUser.getId());

        userDto.setId(savedUser.getId());
        return userDto;
    }

    public String login(UserDto userDto) {
        log.info("Login attempt for user: {}", userDto.getUsername());

        UserEntity user = userRepository.findByUsernameIgnoreCase(userDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: User '{}' not found", userDto.getUsername());
                    return new UserNotFoundException("User didn't find");
                });

        if (passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            log.info("User '{}' successfully logged in", userDto.getUsername());
            return jwtService.generateToken(user.getUsername());
        } else {
            log.warn("Login failed: Wrong password for user '{}'", userDto.getUsername());
            throw new WrongPasswordException("Wrong password!");
        }
    }
}
