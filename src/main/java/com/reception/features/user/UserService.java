package com.reception.features.user;

import com.reception.common.exception.UserNotFoundException;
import com.reception.common.exception.UsernameAlreadyExistsException;
import com.reception.common.exception.WrongPasswordException;
import com.reception.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserDto register(UserDto userDto) {
        if (userRepository.existsByUsernameIgnoreCase(userDto.getUsername())) {
            throw new UsernameAlreadyExistsException("This username has been taken!");
        }

        UserEntity user = UserEntity.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        UserEntity savedUser = userRepository.save(user);

        userDto.setId(savedUser.getId());
        return userDto;
    }

    public String login(UserDto userDto) {

        UserEntity user = userRepository.findByUsernameIgnoreCase(userDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User didn't find"));

        if (passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            return jwtService.generateToken(user.getUsername());
        } else {
            throw new WrongPasswordException("Wrong password!");
        }
    }
}
