package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.users.User;

@Entity
public class ProfileImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
