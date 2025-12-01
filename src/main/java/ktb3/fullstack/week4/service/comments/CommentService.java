package ktb3.fullstack.week4.service.comments;

import ktb3.fullstack.week4.common.error.codes.CommentError;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommentService {

    private final CommentDomainBuilder commentDomainBuilder;

    private final ErrorCheckServiceImpl errorCheckService;

    private final CommentRepository commentRepository;


    // 댓글 등록
    @Transactional
    public void addComment(long userId, long postId, String content) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        Post post = errorCheckService.checkCanNotFoundPost(postId);
        Comment comment = commentDomainBuilder.buildComment(user, post, content);
        comment.linkPost(post);
        commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public void editComment(long userId, long postId, long commentId, String content) {
        errorCheckService.checkCanNotFoundUser(userId);
        errorCheckService.checkCanNotFoundPost(postId);

        if (content == null || content.isBlank()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(CommentError.CANNOT_FOUND_COMMENT));

        if (target.getUser().getId() != userId) {
            throw new ApiException(CommentError.CANNOT_EDIT_OTHERS_COMMENT);
        }

        target.editContent(content);
    }

    // 댓글 삭제
    @Transactional
    public void removeComment(long userId, long postId, long commentId) {
        errorCheckService.checkCanNotFoundUser(userId);
        errorCheckService.checkCanNotFoundPost(postId);

        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(CommentError.CANNOT_FOUND_COMMENT));

        if (target.getUser().getId() != userId) {
            throw new ApiException(CommentError.CANNOT_DELETE_OTHERS_COMMENT);
        }

        target.deleteEntity();
    }
}
