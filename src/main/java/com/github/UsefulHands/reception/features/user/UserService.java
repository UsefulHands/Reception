package com.github.UsefulHands.reception.features.user;

import com.github.UsefulHands.reception.common.audit.AuditLogService;
import com.github.UsefulHands.reception.common.exception.ResourceNotFoundException;
import com.github.UsefulHands.reception.common.exception.UserNotFoundException;
import com.github.UsefulHands.reception.common.exception.UsernameAlreadyExistsException;
import com.github.UsefulHands.reception.common.exception.WrongPasswordException;
import com.github.UsefulHands.reception.common.security.JwtService;
import com.github.UsefulHands.reception.features.admin.AdminDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AuditLogService auditLogService;

    public String login(UserLoginRequest request) {
        log.info("User: {}, trying to login", request.getUsername());

        UserEntity userEntity = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> {
                    log.info("Login failed, {}, user not found", request.getUsername());
                    return new UserNotFoundException("User not found!");
                });

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            log.info("Login failed, {}, wrong password", request.getUsername());
            throw new WrongPasswordException("Invalid credentials!");
        }
        log.info("Login success, {}, user logged in", request.getUsername());

        return jwtService.generateToken(userEntity);
    }

    public UserEntity createAccount(String username, String password, String role) {
        log.info("Creating new User: {}", username);

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException("Username already exists!");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        UserEntity savedEntity = userRepository.save(userEntity);
        log.info("User profile saved with ID: {}", userEntity.getId());

        return savedEntity;
    }

    public List<UserDto> getUsers() {
        log.info("Retrieving users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUser(Long id){
        log.info("Retrieving user");
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}