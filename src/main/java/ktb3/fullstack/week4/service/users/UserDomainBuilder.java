package ktb3.fullstack.week4.service.users;

import ktb3.fullstack.week4.domain.users.Role;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.UserEditPageResponse;
import org.springframework.stereotype.Component;

@Component
public class UserDomainBuilder {

    public User buildUser(String hashedPassword, JoinRequest dto) {
        return User.builder()
                .email(dto.getEmail())
                .hashedPassword(hashedPassword)
                .nickname(dto.getNickname())
                .role(Role.ROLE_USER.toString())
                .build();
    }

    public UserEditPageResponse buildUserPageResponse(String email, String nickname) {
        return UserEditPageResponse.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}
