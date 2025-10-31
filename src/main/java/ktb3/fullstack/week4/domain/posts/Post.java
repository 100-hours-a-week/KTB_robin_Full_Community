package ktb3.fullstack.week4.domain.posts;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.users.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NonNull
    @Column(name = "title")
    private String title;

    @NonNull
    @Column(name = "content")
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostImage> postImages = new ArrayList<>();

    @OneToOne(mappedBy = "post")
    private PostView postView;

    @ManyToOne
    @JoinColumn(name = "id")
    private User user;
}
