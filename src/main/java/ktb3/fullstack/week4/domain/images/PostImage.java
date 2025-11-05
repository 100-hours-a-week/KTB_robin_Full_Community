package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.posts.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
@SQLRestriction("deleted = false") // 모든 조회 쿼리에 자동으로 "where deleted = false" 추가
public class PostImage extends Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;


    public void linkPost(Post post) { // 연관관계 편의 메소드
        this.post = post;
        post.getPostImages().add(this);
    }
}
