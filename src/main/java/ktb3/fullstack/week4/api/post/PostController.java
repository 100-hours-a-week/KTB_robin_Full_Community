package ktb3.fullstack.week4.api.post;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostEditRequest;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.service.comments.CommentService;
import ktb3.fullstack.week4.service.likes.LikeService;
import ktb3.fullstack.week4.service.posts.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestPart PostUploadRequeset dto,
            @RequestPart(required = false) MultipartFile image) {
            postService.uploadPost(userId, dto, image);
            return ApiResponse.ok("post_upload_success");
    }

    @Override
    @GetMapping // ?after=0&limit=5
    public ApiResponse<PostListResponse> getPostList(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @RequestParam(value = "after") int after,
            @RequestParam(value = "limit") int limit) {
        PostListResponse response = postService.getPostList(userId, after, limit);
        return ApiResponse.ok(response, "posts_fetch_success");
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable(value = "id") long postId) {
        PostDetailResponse response = postService.getSinglePostDeatil(userId, postId);
        return ApiResponse.ok(response, "post_fetch_success");
    }

    @Override
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> editPost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId,
            @Valid @RequestPart PostEditRequest dto,
            @RequestPart(required = false) MultipartFile image) {
        postService.editPost(userId, postId, dto, image);
        return ApiResponse.ok("post_edit_success");
    }


    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removePost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        postService.removePost(userId, postId);
        return ApiResponse.ok("post_remove_success");
    }


    @Override
    @PostMapping("/{id}/likes")
    public ApiResponse<Void> addLike(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        likeService.likePost(userId, postId);
        return ApiResponse.ok("post_like_success");
    }


    @Override
    @DeleteMapping("/{id}/likes")
    public ApiResponse<Void> removeLike(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        likeService.unlikePost(userId, postId);
        return ApiResponse.ok("post_unlike_success");
    }


    @Override
    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> addComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @RequestParam("content") String content) {
        commentService.addComment(userId, postId, content);
        return ApiResponse.ok("comment_add_success");
    }

    @Override
    @PatchMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> editComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestParam("content") String content) {
        commentService.editComment(userId, postId, commentId, content);
        return ApiResponse.ok("comment_edit_success");
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> removeComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId) {
        commentService.removeComment(userId, postId, commentId);
        return ApiResponse.ok("comment_remove_success");
    }

}
