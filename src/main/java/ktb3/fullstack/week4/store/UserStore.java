package ktb3.fullstack.week4.store;
import ktb3.fullstack.week4.domain.users.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserStore {

    private final Map<Long, User> userMap = new ConcurrentHashMap<>();
    private final Map<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final Map<String, Long> nicknameIndex = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public long nextId() {
        return seq.incrementAndGet();
    }

    public Optional<User> get(Long id) {
        return Optional.ofNullable(userMap.get(id));
    }

    public List<User> values() {
        return (List<User>) userMap.values();
    }

    // 이메일 중복 검사에 사용
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }

    // 닉네임 중복 검사에 사용
    public boolean existsByNickname(String nickname) {
        return nicknameIndex.containsKey(nickname);
    }

    // 실제 저장소에 엔티티 삽입
    public void put(User user) {
        userMap.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user.getId());
        nicknameIndex.put(user.getNickname(), user.getId());
    }
    
    public void updateNickName(User user, String oldNickname) {
        userMap.put(user.getId(), user);
        nicknameIndex.remove(oldNickname);
        nicknameIndex.put(user.getNickname(), user.getId());
    }

    public void updatePassword(User user) {
        userMap.put(user.getId(), user);
    }

    public void updateProfileImage(User user) {
        userMap.put(user.getId(), user);
    }

    public void deleteProfileImage(User user) {
        userMap.put(user.getId(), user);
    }

    public boolean remove(Long id) {
        User removed = userMap.remove(id);
        if (removed == null) return false;
        emailIndex.remove(removed.getEmail());
        nicknameIndex.remove(removed.getNickname());
        return true;
    }

    // 이메일로 사용자 조회 (로그인 시 실제 userId 사용)
    public Optional<User> getByEmail(String email) {
        Long id = emailIndex.get(email);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(userMap.get(id));
    }
}
