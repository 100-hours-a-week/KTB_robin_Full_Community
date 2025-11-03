package ktb3.fullstack.week4.repository.images;

import ktb3.fullstack.week4.domain.images.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//public interface ProfileImageRepository extends ImageRepository {
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    ProfileImage save(ProfileImage profileImage);

    Optional<ProfileImage> findById(Long id);

    void deleteById(Long id);

    List<ProfileImage> findAllByUserId(Long userId);

    ProfileImage findByUserIdAndIsPrimaryIsTrue(Long userId);

    @Query("select pi " +
            "from ProfileImage pi " +
            "where pi.user.id = :userId and pi.isPrimary = false and pi.deleted = false " +
            "order by pi.displayOrder asc"
    )
    List<ProfileImage> findAllNotPrimary(@Param("userId") Long userId);
}
