package ktb3.fullstack.week4.repository.posts;


import ktb3.fullstack.week4.domain.posts.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Post save(Post post);

    Optional<Post> findById(Long id);

    List<Post> findAllByUserId(Long userId);

    void deleteById(Long id);

    List<Post> findAllByIdBetween(Long idAfter, Long idBefore);
}
