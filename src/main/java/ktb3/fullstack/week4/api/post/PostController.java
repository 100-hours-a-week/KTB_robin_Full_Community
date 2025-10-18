package ktb3.fullstack.week4.api.post;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 등록
    // Content-Type: multipart/form-data
    @PostMapping
    public ApiResponse<Void> uploadPost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestPart PostUploadRequeset dto,
            @RequestPart MultipartFile image) {
            postService.uploadPost(userId, dto, image);
            return ApiResponse.ok("post_upload_success");
    }

    // 게시글 목록 조회
    @GetMapping // ?after=0&limit=5
    public ApiResponse<PostListResponse> getPostList(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @RequestParam(value = "after") int after,
            @RequestParam(value = "limit") int limit) {
        PostListResponse response = postService.getPostList(userId, after, limit);
        return ApiResponse.ok(response, "posts_fetch_success");
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable(value = "id") long postId) {
        PostDetailResponse response = postService.getSinglePostDeatil(userId, postId);
        return ApiResponse.ok(response, "post_fetch_success");
    }

    // 게시글 수정
    // Content-Type: multipart/form-data
    @PatchMapping("/{id}")
    public ApiResponse<Void> editPost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId,
            @Valid @RequestPart PostUploadRequeset dto,
            @RequestPart(required = false) MultipartFile image) {
        postService.editPost(userId, postId, dto, image);
        return ApiResponse.ok("post_edit_success");
    }


    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> removePost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        postService.removePost(userId, postId);
        return ApiResponse.ok("post_remove_success");
    }


    // 게시글 좋아요
    @PostMapping("/{id}/likes")
    public ApiResponse<Void> addLike(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        postService.likePost(userId, postId);
        return ApiResponse.ok("post_like_success");
    }


    // 게시글 좋아요 취소
    @DeleteMapping("/{id}/likes")
    public ApiResponse<Void> removeLike(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        postService.unlikePost(userId, postId);
        return ApiResponse.ok("post_unlike_success");
    }


    // 게시글 댓글 등록
    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> addComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @RequestParam("content") String content) {
        postService.addComment(userId, postId, content);
        return ApiResponse.ok("comment_add_success");
    }

    // 게시글 댓글 수정
    @PatchMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> editComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestParam("content") String content) {
        postService.editComment(userId, postId, commentId, content);
        return ApiResponse.ok("comment_edit_success");
    }

    // 게시글 댓글 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> removeComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId) {
        postService.removeComment(userId, postId, commentId);
        return ApiResponse.ok("comment_remove_success");
    }

}
