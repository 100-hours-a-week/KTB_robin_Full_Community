package ktb3.fullstack.week4.service.posts;

import ktb3.fullstack.week4.common.error.codes.FileError;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.codes.PostError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.posts.PostView;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.*;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.repository.images.PostImageRepository;
import ktb3.fullstack.week4.repository.images.ProfileImageRepository;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.posts.PostViewRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import ktb3.fullstack.week4.service.images.ImageDomainBuilder;
import ktb3.fullstack.week4.service.images.PostImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostDomainBuilder postDomainBuilder;
    private final ImageDomainBuilder imageDomainBuilder;

    private final ErrorCheckServiceImpl errorCheckService;

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final PostImageRepository postImageRepository;
    private final ProfileImageRepository profileImageRepository;

    private final PostDeleteFacade postDeleteFacade;
    private final PostImageService postImageService;


    // 게시글 등록
    @Transactional
    public void uploadPost(long userId, PostUploadRequeset dto, MultipartFile image) {
        User user = errorCheckService.checkCanNotFoundUser(userId);

        Post post = postDomainBuilder.buildPost(dto, user);
        postRepository.save(post);

        PostView postView = postDomainBuilder.buildPostView(post);
        postViewRepository.save(postView);

        if (image != null) {
            String postImageUrl = postImageService.makeImagePathString(image);
            postImageService.transferImageToLocalDirectory(image, postImageUrl);

            String savedUrl = postImageUrl.split("static")[1];
            PostImage postImage = imageDomainBuilder.buildPostImage(post, savedUrl);

            postImageRepository.save(postImage);
        }
    }

    // 게시글 목록조회
    @Transactional(readOnly = true)
    public PostListResponse getPostList(long userId, int after, int limit) {
        errorCheckService.checkCanNotFoundUser(userId);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"));

        Slice<Post> postSlice = postRepository.findByIdGreaterThan((long) after, pageable);

        // 일반적으로 말하는 N+1 문제는 아니지만, 필요한 데이터에 비해 너무 많은 쿼리가 나가는중.
        // 이제 Slice 사이즈가 m개 라면, likes, comments, views, user, userImage 를 가져오기 위한 쿼리 5종이 m번씩 발생하게됨
        // 1 + m*5 번의 쿼리가 발생 : 분명 개선할 필요가 있는 문제.
        List<PostListResponse.PostBriefInfo> briefs = postSlice.getContent().stream()
                .map(post -> {
                    long postId = post.getId();

                    long likes = likeRepository.countByPostId(postId);
                    long comments = commentRepository.countByPostId(postId);
                    long views = post.getPostView().getViewCount();

                    User author = post.getUser();
                    String authorName = author.getNickname();
                    String authorProfileImageUrl = profileImageRepository.findByUserIdAndIsPrimaryIsTrue(author.getId()).getImageUrl();

                    return new PostListResponse.PostBriefInfo(
                            post.getId(),
                            post.getTitle(),
                            likes,
                            comments,
                            views,
                            authorName,
                            authorProfileImageUrl,
                            post.getModifiedAt()
                    );
                })
                .toList();

        int nextCursor = briefs.isEmpty() ? after : Math.toIntExact(briefs.getLast().getId());

        return new PostListResponse(briefs, nextCursor, postSlice.hasNext());
    }

    // 게시글 상세조회
    @Transactional
    public PostDetailResponse getSinglePostDeatil(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId);

        if (postId <= 0) { // 컨트롤러 단에서 @Valid로 검증하도록 수정 필요
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        PostDetailDto dto = postRepository.findPostDetailByIdAndUserId(postId, userId)
                .orElseThrow(() -> new ApiException(PostError.CANNOT_FOUND_POST));

        postViewRepository.findById(postId).ifPresent(PostView::plusViewCount);

        boolean isLiked = likeRepository.existsByPostIdAndUserIdAndIsLikedTrue(postId, userId);

        String primaryImageUrl = postImageRepository.findByPostIdAndIsPrimaryIsTrue(postId)
                .map(PostImage::getImageUrl)
                .orElseThrow(() -> new ApiException(FileError.IMAGE_NOT_FOUND));

        List<String> restImageUrls = postImageRepository.findAllNotPrimaryPostImages(postId)
                .stream().map(PostImage::getImageUrl)
                .toList();

        boolean isOwner = dto.getAuthorId() == userId;

        PostDetailResponse.PostInfo postInfo = new PostDetailResponse.PostInfo(
                dto.getId(),
                dto.getTitle(),
                dto.getLikeCount(),
                dto.getCommentCount(),
                dto.getViewCount() + 1,
                dto.getAuthorNickname(),
                dto.getAuthorProfileImageUrl(),
                dto.getModifiedAt(),
                primaryImageUrl,
                restImageUrls,
                dto.getContent()
        );

        return new PostDetailResponse(postInfo, isLiked, isOwner);
    }

    @Transactional
    public void editPost(long userId, long postId, PostEditRequest dto, MultipartFile image) {
        errorCheckService.checkCanNotFoundUser(userId);

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        errorCheckService.checkCanNotEditOthersPost(userId, post.getUser().getId());

        post.editPost(dto);

        if (image != null && !image.isEmpty()) {
            postImageRepository.findByPostIdAndIsPrimaryIsTrue(postId)
                    .ifPresent(SoftDeletetionEntity::deleteEntity);

            String postImageUrl = postImageService.makeImagePathString(image);
            postImageService.transferImageToLocalDirectory(image, postImageUrl);

            String savedUrl = postImageUrl.split("static")[1];
            PostImage postImage = imageDomainBuilder.buildPostImage(post, savedUrl);

            postImageRepository.save(postImage);
            post.renewModifiedAt(postImage.getModifiedAt());
        }
    }

    // 게시글 삭제
    @Transactional
    public void removePost(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId);

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 본인 소유 확인
        errorCheckService.checkCanNotDeleteOthersPost(userId, post.getUser().getId());

        postDeleteFacade.deletePost(postId);
    }
}
