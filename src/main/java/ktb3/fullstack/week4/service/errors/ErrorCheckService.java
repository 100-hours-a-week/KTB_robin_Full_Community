package ktb3.fullstack.week4.service.errors;

import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;

public interface ErrorCheckService {

    User checkCanNotFoundUser(long userId);

    Post checkCanNotFoundPost(long postId);

    void checkCanNotEditOthersPost(long userId, long authorId);

    void checkCanNotDeleteOthersPost(long userId, long authorId);

    Like checkCanNotFoundLike(long postId, long userId);
}
