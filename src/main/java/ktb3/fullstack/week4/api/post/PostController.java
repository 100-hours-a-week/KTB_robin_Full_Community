package ktb3.fullstack.week4.api.post;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.common.error.codes.*;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController implements PostApi {
    private final PostService postService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadPost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Parameter(
                    name = "dto",
                    description = "게시글 내용(JSON)",
                    required = true,
                    content = @Content(mediaType = "application/json")
            )
            @Valid @RequestPart PostUploadRequeset dto,
            @Parameter(
                    name = "image",
                    description = "게시글 이미지 파일(선택)",
                    required = false,
                    content = @Content(mediaType = "multipart/form-data")
            )
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
    @PatchMapping("/{id}")
    public ApiResponse<Void> editPost(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId,
            @Valid @RequestPart PostUploadRequeset dto,
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
        postService.likePost(userId, postId);
        return ApiResponse.ok("post_like_success");
    }


    @Override
    @DeleteMapping("/{id}/likes")
    public ApiResponse<Void> removeLike(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("id") long postId) {
        postService.unlikePost(userId, postId);
        return ApiResponse.ok("post_unlike_success");
    }


    @Override
    @PostMapping("/{postId}/comments")
    public ApiResponse<Void> addComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @RequestParam("content") String content) {
        postService.addComment(userId, postId, content);
        return ApiResponse.ok("comment_add_success");
    }

    @Override
    @PatchMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> editComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestParam("content") String content) {
        postService.editComment(userId, postId, commentId, content);
        return ApiResponse.ok("comment_edit_success");
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> removeComment(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId) {
        postService.removeComment(userId, postId, commentId);
        return ApiResponse.ok("comment_remove_success");
    }

}
