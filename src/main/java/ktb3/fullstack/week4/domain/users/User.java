package ktb3.fullstack.week4.domain.users;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
public class User extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "hashed_password")
    private String hashedPassword;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user")
    private List<ProfileImage> profileImages = new ArrayList<>();

    public void addProfileImage(ProfileImage profileImage) { // 연관관계 편의 메소드
        profileImages.add(profileImage);
        profileImage.linkUser(this);
    }

    public void changeNickName(String newNickname) {
        nickname = newNickname;
    }
    public void changePassword(String newPassword) {
        hashedPassword = newPassword;
    }
}
