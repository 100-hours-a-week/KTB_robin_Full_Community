package ktb3.fullstack.week4.service.errors;

import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.codes.PostError;
import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ErrorCheckServiceImpl implements ErrorCheckService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional(readOnly = true)
    public User checkCanNotFoundUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Post checkCanNotFoundPost(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(PostError.CANNOT_FOUND_POST));
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public Like checkCanNotFoundLike(long postId, long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId);
        if(like == null) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        return like;
    }

    @Override
    public void checkCanNotEditOthersPost(long userId, long authorId) {
        if(userId != authorId) {
            throw new ApiException(PostError.CANNOT_EDIT_OTHERS_POST);
        }
    }

    @Override
    public void checkCanNotDeleteOthersPost(long userId, long authorId) {
        if(userId != authorId) {
            throw new ApiException(PostError.CANNOT_DELETE_OTHERS_POST);
        }
    }
}
