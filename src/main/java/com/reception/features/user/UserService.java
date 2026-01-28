package com.reception.features.user;

import com.reception.common.security.JwtService;
import com.reception.features.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Token üretmek için ekledik

    public String register(UserDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        UserEntity user = UserEntity.builder()
                .username(userDto.getUsername())
                .password(encodedPassword)
                .build();
        userRepository.save(user);
        return "Kullanıcı başarıyla kaydedildi!";
    }

    public String login(UserDto userDto) {
        // 1. Kullanıcıyı veritabanında ara
        UserEntity user = userRepository.findByUsername(userDto.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // 2. Gelen şifre ile veritabanındaki (hashlenmiş) şifreyi karşılaştır
        if (passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            // 3. Şifre doğruysa Token üret ve dön
            return jwtService.generateToken(user.getUsername());
        } else {
            throw new RuntimeException("Hatalı şifre!");
        }
    }
}
