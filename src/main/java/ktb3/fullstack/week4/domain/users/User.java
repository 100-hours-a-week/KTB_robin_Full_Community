package ktb3.fullstack.week4.domain.users;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "hashed_password")
    private String hashedPassword;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void changeNickName(String newNickname) {
        nickname = newNickname;
    }
    public void changePassword(String newPassword) {
        hashedPassword = newPassword;
    }
}
