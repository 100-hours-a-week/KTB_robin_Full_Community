package ktb3.fullstack.week4.domain.users;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class User {
    private long id;
    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;

    public void changeNickName(String newNickname) {
        nickname = newNickname;
    }
    public void changePassword(String newPassword) {
        password = newPassword;
    }
    public void changeProfileImage(String newProfileImageUrl) {
        profileImageUrl = newProfileImageUrl;
    }
}
