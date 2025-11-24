package ktb3.fullstack.week4.domain.users;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@Table(name = "users")
@SQLRestriction("deleted = false") // 모든 조회 쿼리에 자동으로 "where deleted = false" 추가
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

    @Column(name = "role", columnDefinition = "VARCHAR(10) default 'USER'")
    private String role;

    @Builder.Default
    @SQLRestriction("deleted = false") // 모든 조회 쿼리에 자동으로 "where deleted = false" 추가
    @OneToMany(mappedBy = "user")
    private List<ProfileImage> profileImages = new ArrayList<>();


    public void changeNickName(String newNickname) {
        nickname = newNickname;
    }

    public void changePassword(String newPassword) {
        hashedPassword = newPassword;
    }
}
