package ktb3.fullstack.week4.repository.posts;


import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.dto.posts.PostDetailDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Post save(Post post);

    Optional<Post> findById(Long id);
    List<Post> findAllByUserId(Long userId);

    void deleteById(Long id);

    Slice<Post> findByIdGreaterThan(Long id, Pageable pageable);

    @Query("SELECT new ktb3.fullstack.week4.dto.posts.PostDetailDto(" +
            "   p.id, p.title, p.content, p.modifiedAt, " +
            "   u.id, u.nickname, pi.imageUrl, " +
            "   pv.viewCount, " +
            "   (SELECT COUNT(l) FROM Like l WHERE l.post.id = p.id AND l.isLiked = true), " +
            "   (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id), " +
            "   CASE WHEN (SELECT COUNT(l2) FROM Like l2 WHERE l2.post.id = p.id AND l2.user.id = :userId AND l2.isLiked = true) > 0 THEN true ELSE false END " +
            ") " +
            "FROM Post p " +
            "JOIN p.user u " +
            "LEFT JOIN ProfileImage pi ON pi.user.id = u.id AND pi.isPrimary = true " +
            "LEFT JOIN p.postView pv " +
            "WHERE p.id = :postId")
    Optional<PostDetailDto> findPostDetailByIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
