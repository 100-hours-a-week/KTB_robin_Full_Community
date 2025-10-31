package ktb3.fullstack.week4.domain.posts;

import jakarta.persistence.*;
import ktb3.fullstack.week4.domain.BaseTimeEntity;
import lombok.Getter;

@Entity
@Getter
public class PostView extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "view_count")
    private long viewCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @MapsId
    private Post post;
}
