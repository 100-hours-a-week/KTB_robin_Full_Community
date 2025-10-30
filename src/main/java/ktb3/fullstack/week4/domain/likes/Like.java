package ktb3.fullstack.week4.domain.likes;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.users.User;

@Entity
public class Like extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "is_liked")
    private boolean isLiked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
