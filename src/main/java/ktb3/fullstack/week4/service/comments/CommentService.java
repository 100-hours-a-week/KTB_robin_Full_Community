package ktb3.fullstack.week4.service.comments;

import ktb3.fullstack.week4.common.error.codes.CommentError;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.util.CustomDateTimeFormatter;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.CommentListResponse;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {

    private final CommentDomainBuilder commentDomainBuilder;

    private final ErrorCheckServiceImpl errorCheckService;

    private final CommentRepository commentRepository;

    private final CustomDateTimeFormatter customDateTimeFormatter;


    // 댓글 등록
    @Transactional
    public void addComment(long userId, long postId, String content) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        Post post = errorCheckService.checkCanNotFoundPost(postId);
        Comment comment = commentDomainBuilder.buildComment(user, post, content);
        comment.linkPost(post);
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public CommentListResponse getCommentList(long userId, long postId, String modifiedBefore, Long cursorId, int limit) {
        errorCheckService.checkCanNotFoundUser(userId);

        LocalDateTime time;
        if (modifiedBefore == null || cursorId == null) {
            time = LocalDateTime.now().plusYears(100);
            cursorId = Long.MAX_VALUE;
        } else {
            time = customDateTimeFormatter.format(modifiedBefore);
        }

        Pageable pageable = PageRequest.of(0, limit);

        Slice<Comment> commentSlice = commentRepository.findByModifiedAtLessThan(time, postId, cursorId, pageable);
        List<CommentListResponse.CommentInfo> commentInfos = commentSlice.getContent().stream()
                .map(comment -> new CommentListResponse.CommentInfo(
                        comment.getId(),
                        comment.getUser().getNickname(),
                        comment.getContent(),
                        comment.getUser().getProfileImages().getFirst().getImageUrl(),
                        comment.getModifiedAt()
                )).toList();

        Long nextCursorId = commentInfos.isEmpty() ? null : commentInfos.getLast().getId()-1;

        return new CommentListResponse(commentInfos, nextCursorId, commentSlice.hasNext());
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
