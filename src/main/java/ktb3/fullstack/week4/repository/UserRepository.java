package ktb3.fullstack.week4.repository;

import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.store.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository implements CrudRepository<User, Long> {

    private final UserStore store;

    public boolean existsByEmail(String email) {
        return store.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return store.existsByNickname(nickname);
    }

    @Override
    public User save(User entity) {
        if (entity.getId() != 0) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        long id = store.nextId();
        entity.setId(id);
        store.put(entity);
        return entity;
    }

    // 이메일로 사용자 조회 (AuthService.login에서 사용)
    public Optional<User> findByEmail(String email) {
        return store.getByEmail(email);
    }

    public void updateNickname(User user, String oldNickname) {
        store.updateNickName(user, oldNickname);
    }

    public void updatePassword(User user) {
        store.updatePassword(user);
    }

    public void updateProfileImage(User user) {
        store.updateProfileImage(user);
    }

    public void deleteProfileImage(User user) {
        store.deleteProfileImage(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return store.get(id);
    }

    @Override
    public List<User> findAll() {
        return store.values();
    }

    @Override
    public boolean deleteById(Long id) {
        return store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.get(id).isPresent();
    }
}