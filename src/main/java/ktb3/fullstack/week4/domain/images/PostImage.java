package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.posts.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
public class PostImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public void linkPost(Post post) {
        this.post = post;
    }
}
