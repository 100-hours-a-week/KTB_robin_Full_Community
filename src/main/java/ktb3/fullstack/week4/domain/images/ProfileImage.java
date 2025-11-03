package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.users.User;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
public class ProfileImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void linkUser(User user) {
        this.user = user;
    }
}
