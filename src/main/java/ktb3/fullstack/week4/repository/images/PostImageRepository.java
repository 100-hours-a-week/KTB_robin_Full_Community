package ktb3.fullstack.week4.repository.images;

import ktb3.fullstack.week4.domain.images.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//public interface ProfileImageRepository extends ImageRepository {
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    PostImage save(PostImage postImage);

    Optional<PostImage> findById(Long id);

    void deleteById(Long id);

    List<PostImage> findAllByPostId(Long postId);
}
