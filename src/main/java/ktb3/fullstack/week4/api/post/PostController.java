package ktb3.fullstack.week4.api.post;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.Security.context.SecurityUser;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.posts.*;
import ktb3.fullstack.week4.service.comments.CommentService;
import ktb3.fullstack.week4.service.likes.LikeService;
import ktb3.fullstack.week4.service.posts.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CommonErrorResponses
@AccessTokenExpireResponse
public class PostController implements PostApi {
    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadPost(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestPart PostUploadRequeset dto,
            @RequestPart(required = false) MultipartFile image) {
            postService.uploadPost(user.getId(), dto, image);
            return ApiResponse.ok("post_upload_success");
    }

    @Override
    @GetMapping // ?after=0&limit=5
    public ApiResponse<PostListResponse> getPostList(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(value = "after") int after,
            @RequestParam(value = "limit") int limit) {
        PostListResponse response = postService.getPostList(user.getId(), after, limit);
        return ApiResponse.ok(response, "posts_fetch_success");
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable(value = "id") long postId) {
        PostDetailResponse response = postService.getSinglePostDeatil(user.getId(), postId);
        return ApiResponse.ok(response, "post_fetch_success");
    }

    @Override
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> editPost(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("id") long postId,
            @Valid @RequestPart PostEditRequest dto,
            @RequestPart(required = false) MultipartFile image) {
        postService.editPost(user.getId(), postId, dto, image);
        return ApiResponse.ok("post_edit_success");
    }


    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removePost(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("id") long postId) {
        postService.removePost(user.getId(), postId);
        return ApiResponse.ok("post_remove_success");
    }


    @Override
    @PostMapping("/{id}/likes")
    public ApiResponse<Void> addLike(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("id") long postId) {
        likeService.likePost(user.getId(), postId);
        return ApiResponse.ok("post_like_success");
    }


    @Override
    @DeleteMapping("/{id}/likes")
    public ApiResponse<Void> removeLike(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("id") long postId) {
        likeService.unlikePost(user.getId(), postId);
        return ApiResponse.ok("post_unlike_success");
    }


    @Override
    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> addComment(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("postId") long postId,
            @RequestParam("content") String content) {
        commentService.addComment(user.getId(), postId, content);
        return ApiResponse.ok("comment_add_success");
    }

    @Override
    @GetMapping("/{postId}/comments")
    public ApiResponse<CommentListResponse> getCommentList(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
            @PathVariable("postId") long postId,
            @RequestParam(value = "modifiedBefore", required = false) String modifiedBefore,
            @RequestParam(value = "cursorId", required = false) Long cursorId,
            @RequestParam(value = "limit") int limit) {
        CommentListResponse response = commentService.getCommentList(user.getId(), postId, modifiedBefore, cursorId, limit);
        return ApiResponse.ok(response, "comments_fetch_success");
    }

    @Override
    @PatchMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> editComment(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestParam("content") String content) {
        commentService.editComment(user.getId(), postId, commentId, content);
        return ApiResponse.ok("comment_edit_success");
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> removeComment(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId) {
        commentService.removeComment(user.getId(), postId, commentId);
        return ApiResponse.ok("comment_remove_success");
    }

}
