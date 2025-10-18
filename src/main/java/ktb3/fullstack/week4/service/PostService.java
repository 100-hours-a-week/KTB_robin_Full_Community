package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.image.ImageProcessor;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.repository.posts.CommentRepository;
import ktb3.fullstack.week4.repository.posts.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.UserRepository;
import ktb3.fullstack.week4.repository.posts.ViewRepository;
import ktb3.fullstack.week4.store.images.PostImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageProcessor imageProcessor;
    private final PostImageStore postImageStore;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ViewRepository viewRepository;

    // 게시글 등록
    public void uploadPost(long userId, PostUploadRequeset dto, MultipartFile image) {
        checkCanNotFoundUser(userId);

        String postImageUrl = null;
        if(image != null) {
            byte[] imageBytes = imageProcessor.toByteStream(image);
            postImageUrl = imageProcessor.makeRandomImageUrl();
            postImageStore.uploadImage(postImageUrl, imageBytes);
        }

        long authorId = userId;
        String title = dto.getTitle();
        String content = dto.getContent();

        Post post = new Post(
                0L,
                authorId,
                title,
                content,
                postImageUrl,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        postRepository.save(post);
    }

    // 게시글 목록조회
    public PostListResponse getPostList(long userId, int after, int limit) {
        checkCanNotFoundUser(userId);

        int from = after + 1;
        int to = after + limit;

        List<Post> raw = postRepository.findPosts(from, to);

        boolean hasNext = raw.size() > limit;
        List<Post> page = hasNext ? raw.subList(0, limit) : raw;

        List<PostListResponse.PostBriefInfo> briefs = new ArrayList<>(page.size());

        for (Post post : page) {
            long likes = likeRepository.countByPostId(post.getId());
            long comments = commentRepository.countByPostId(post.getId());
            long views = viewRepository.countByPostId(post.getId());

            String author = userRepository.findById(post.getAuthorId())
                    .map(User::getNickname)
                    .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));

            briefs.add(new PostListResponse.PostBriefInfo(
                    post.getId(),
                    post.getTitle(),
                    likes,
                    comments,
                    views,
                    author,
                    post.getModifiedAt()
            ));
        }

        int nextCursor = page.isEmpty() ?
                after :
                Math.toIntExact(page.get(page.size() - 1).getId());

        return new PostListResponse(briefs, nextCursor, hasNext);
    }

//    // 게시글 상세조회
//    public PostDetailResponse getSinglePostDeatil(long userId, long postId) {
//
//    }

    private User checkCanNotFoundUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        return user;
    }
}
