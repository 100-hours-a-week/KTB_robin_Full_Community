package ktb3.fullstack.week4.repository.posts;

import ktb3.fullstack.week4.domain.posts.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    PostView save(PostView postView);

    Optional<PostView> findById(Long id);

    @Query("select pv.viewCount from PostView pv where pv.post.id =:postId")
    long viewCountByPostId(Long postId);

}
