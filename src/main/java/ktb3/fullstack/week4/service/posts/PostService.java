package ktb3.fullstack.week4.service.posts;

import ktb3.fullstack.week4.common.error.codes.*;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.posts.PostView;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostEditRequest;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

        List<PostListResponse.PostBriefInfo> briefs = postSlice.getContent().stream()
                .map(post -> {
                    long postId = post.getId();

                    long likes = likeRepository.countByPostId(postId);
                    long comments = commentRepository.countByPostId(postId);
                    long views = postViewRepository.viewCountByPostId(postId);

                    User author = errorCheckService.checkCanNotFoundUser(post.getUser().getId());
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

        if (postId <= 0) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 조회 수 증가
        PostView postView = post.getPostView();
        postView.plusViewCount();

        User authorEntity = errorCheckService.checkCanNotFoundUser(post.getUser().getId());
        String author = authorEntity.getNickname();
        String authorProfileImageUrl = profileImageRepository.findByUserIdAndIsPrimaryIsTrue(authorEntity.getId()).getImageUrl();

        // 숫자 정보
        long likes = likeRepository.countByPostId(postId);
        long commentsCount = commentRepository.countByPostId(postId);
        long views = postViewRepository.viewCountByPostId(postId);

        // (사용자 - 현재 조회중인 게시물) 좋아요 누름 여부
        boolean isLiked = likeRepository.existsByPostIdAndUserIdAndIsLikedTrue(postId, userId);

        boolean isOwner = post.getUser().getId() == userId;

        // 댓글 상세 목록 조회 + dto 매핑
        List<Comment> commentEntities = commentRepository.findAllByPostId(postId);
        List<PostDetailResponse.CommentInfo> comments = new ArrayList<>();
        for (Comment comment : commentEntities) {
            String commentAuthorProfileImageUrl = profileImageRepository.findByUserIdAndIsPrimaryIsTrue(comment.getUser().getId()).getImageUrl();
            comments.add(new PostDetailResponse.CommentInfo(
                            comment.getId(),
                            comment.getUser().getNickname(),
                            comment.getContent(),
                            commentAuthorProfileImageUrl,
                            comment.getModifiedAt()
                    )
            );
        }

        String primaryImageUrl = "";
        try {
            PostImage primaryImage = postImageRepository.findByPostIdAndIsPrimaryIsTrue(postId).get();
            primaryImageUrl = primaryImage.getImageUrl();
        } catch (NoSuchElementException e) {
            log.info("사진이 없음");
        }

        List<PostImage> restImages = postImageRepository.findAllNotPrimaryPostImages(postId);
        List<String> restImageUrls = new ArrayList<>();
        for (PostImage postImage : restImages) {
            restImageUrls.add(postImage.getImageUrl());
        }

        PostDetailResponse.PostInfo postInfo = new PostDetailResponse.PostInfo(
                post.getId(),
                post.getTitle(),
                likes,
                commentsCount,
                views,
                author,
                authorProfileImageUrl,
                post.getModifiedAt(),
                primaryImageUrl,
                restImageUrls,
                post.getContent()
        );

        return new PostDetailResponse(postInfo, isLiked, isOwner, comments);
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
