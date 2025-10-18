package ktb3.fullstack.week4.repository.posts;

public interface PostSocialInfoRepository {
    long plusCount(long postId);
    long countByPostId(long postId);
}

