package com.github.UsefulHands.reception.features.user;

import com.github.UsefulHands.reception.common.exception.*;
import com.github.UsefulHands.reception.common.security.JwtService;
import com.github.UsefulHands.reception.features.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public String login(UserLoginRequest request) {
        log.info("User: {}, trying to login", request.getUsername());

        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> {
                    log.info("Login failed, user not found: {}", request.getUsername());
                    return new UserNotFoundException("User not found!");
                });

        if (userEntity.isDeleted()) {
            log.warn("Login attempt for deleted account: {}", request.getUsername());
            throw new RuntimeException("Account is disabled or deleted!");
        }

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            log.info("Login failed, wrong password for user: {}", request.getUsername());
            throw new WrongPasswordException("Invalid credentials!");
        }

        log.info("Login success for user: {}", request.getUsername());

        return jwtService.generateToken(userEntity);
    }

    public UserEntity createAccount(String username, String password, String role) {
        log.info("Creating or re-activating user account: {}", username);
        try {
            return userRepository.findByUsernameIgnoreCase(username)
                    .map(existingUser -> {
                        if (!existingUser.isDeleted()) {
                            throw new UsernameAlreadyExistsException("Username already exists and is active!");
                        }
                        log.info("Re-activating deleted user: {}", username);
                        existingUser.setPassword(passwordEncoder.encode(password));
                        existingUser.setRole(role);
                        existingUser.setDeleted(false);
                        return userRepository.saveAndFlush(existingUser); // Anında yaz ki catch yakalayabilsin
                    })
                    .orElseGet(() -> {
                        log.info("Creating brand new account: {}", username);
                        UserEntity newUser = UserEntity.builder()
                                .username(username)
                                .password(passwordEncoder.encode(password))
                                .role(role)
                                .isDeleted(false)
                                .build();
                        return userRepository.saveAndFlush(newUser);
                    });
        } catch (DataIntegrityViolationException e) {
            log.error("Database unique constraint violation for username: {}", username);
            throw new DataIntegrityException("Username already taken by another process!");
        }
    }

    public List<UserDto> getUsers() {
        log.info("Retrieving all active users");
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.isDeleted())
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUser(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with id: " + id));
    }

    public void changePassword(UserEntity user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new WrongPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}