package ktb3.fullstack.week4.Security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class AppPasswordEncoder {

    private final PasswordEncoder passwordEncoder;

    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}