package ktb3.fullstack.week4.repository.comments;

import ktb3.fullstack.week4.domain.comments.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment save(Comment comment);

    void deleteById(Long id);

    long countByPostId(Long postId);

    List<Comment> findAllByPostId(Long postId);

    List<Comment> findAllByUserId(Long userId);
}
