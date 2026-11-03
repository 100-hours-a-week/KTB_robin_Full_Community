package ktb3.fullstack.week4.repository.comments;

import ktb3.fullstack.week4.domain.comments.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment save(Comment comment);

    void deleteById(Long id);

    long countByPostId(Long postId);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.post.id = :postId " +
            "AND (c.modifiedAt < :modifiedAt " +
            "OR (c.modifiedAt = :modifiedAt AND c.id < :cursorId)) " +
            "ORDER BY c.modifiedAt DESC, c.id DESC")
    Slice<Comment> findByModifiedAtLessThan(@Param("modifiedAt") LocalDateTime modifiedAt, @Param("postId") Long postId, @Param("cursorId") Long cursorId, Pageable pageable);

    List<Comment> findAllByUserId(Long userId);
}
