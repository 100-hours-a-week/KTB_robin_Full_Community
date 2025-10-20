package ktb3.fullstack.week4.repository.posts;


import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.repository.CrudRepository;
import ktb3.fullstack.week4.store.posts.PostStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository implements CrudRepository<Post, Long> {
    private final PostStore postStore;

    @Override
    public Post save(Post entity) {
        long id = postStore.nextId();
        entity.setId(id);
        postStore.put(entity);
        return entity;
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(postStore.get(id));
    }

    @Override
    public List<Post> findAll() {
        return List.of();
    }

    @Override
    public boolean deleteById(Long id) {
        return postStore.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    // 목록 조회 (id가 from - to)
    public List<Post> findPosts(int from, int to) {
        List<Post> result = new ArrayList<>();
        // has_next 까지 Service 에서 판단할 수 있도록,
        // 게시글을 페이징 크기보다 1개 더 담음
        int endInclusiveWithExtra = to + 1;
        for (int id = from; id <= endInclusiveWithExtra; id++) {
            Post post = postStore.get((long) id);
            if (post != null) {
                result.add(post);
            }
        }
        return result;
    }

    // 게시글 업데이트
    public Post update(Post entity) {
        Post existing = postStore.get(entity.getId());
        if (existing == null) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        postStore.put(entity);
        return entity;
    }


}
