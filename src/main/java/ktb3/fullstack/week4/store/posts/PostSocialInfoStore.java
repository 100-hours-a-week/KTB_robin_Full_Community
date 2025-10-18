package ktb3.fullstack.week4.store.posts;


public interface PostSocialInfoStore {
    long increment(long postId);
    long getCount(long postId);
}
