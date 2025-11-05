package ktb3.fullstack.week4.repository.images;

import ktb3.fullstack.week4.domain.images.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    PostImage save(PostImage postImage);

    Optional<PostImage> findById(Long id);

    void deleteById(Long id);


    Optional<PostImage> findByPostIdAndIsPrimaryIsTrue(Long postId);

    @Query("select pi " +
            "from PostImage pi " +
            "where pi.post.id = :postId and pi.isPrimary = false and pi.deleted = false " +
            "order by pi.displayOrder asc"
    )
    List<PostImage> findAllNotPrimaryPostImages(@Param("postId") Long postId);
}
