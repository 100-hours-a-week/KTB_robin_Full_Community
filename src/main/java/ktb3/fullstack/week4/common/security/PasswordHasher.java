package ktb3.fullstack.week4.common.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    // BCrypt cost(라운드): 10~12 권장 (서버 성능/지연과 트레이드오프)
    private final int logRounds;

    public PasswordHasher() {
        this(12);
    }

    public PasswordHasher(int logRounds) {
        this.logRounds = logRounds;
    }

    public String hash(String rawPassword) {
        String salt = BCrypt.gensalt(logRounds);
        return BCrypt.hashpw(rawPassword, salt);
    }

    public boolean matches(String rawPassword, String hashed) {
        // BCrypt.checkpw 내부가 상수시간 비교를 사용
        return BCrypt.checkpw(rawPassword, hashed);
    }
}
