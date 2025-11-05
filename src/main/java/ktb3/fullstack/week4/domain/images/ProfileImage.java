package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.users.User;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@SQLRestriction("deleted = false") // 모든 조회 쿼리에 자동으로 "where deleted = false" 추가
public class ProfileImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public void linkUser(User user) { // 연관관계 편의 메소드
        this.user = user;
        user.getProfileImages().add(this);
    }
}
