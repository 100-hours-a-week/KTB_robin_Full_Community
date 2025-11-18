// 게시글 목록 페이지 JS
import {fetchPosts} from "../features/posts/api.js";
import {setAvatar} from "../components/Avatar.js";

// 상태
const PAGE_SIZE = 5;
let cursor = 0;
let hasNext = true;
let isLoading = false;

// 엘리먼트
const $list = document.querySelector(".feed-list");
const $loader = document.querySelector(".feed-loader");
const $sentinel = document.getElementById("infinite-scroll-sentinel");
const $template = document.getElementById("post-card-template");

// 유틸
function truncate(text, max) {
    if (!text) return "";
    return text.length <= max ? text : text.slice(0, max);
}

function formatDateTime(isoLike) {
    if (!isoLike) return "";
    // 서버는 "yyyy-MM-ddTHH:mm:ss" 형태를 반환한다고 가정
    // 요구 포맷: "yyyy-mm-dd hh:mm:ss"
    return String(isoLike).replace("T", " ").slice(0, 19);
}

function formatCount(n) {
    const num = Number(n || 0);
    if (num < 1000) return String(num);
    // 1,000 => 1k, 12,345 => 12k, 100,000 => 100k
    return `${Math.floor(num / 1000)}k`;
}

function createPostCard(post) {
    const {id, title, likeCount, commentCount, viewCount, author, authorProfileImageUrl, modified_at} = post;
    const $article = $template.content.firstElementChild.cloneNode(true);

    // 카드 식별용 데이터 속성 (이벤트 위임에서 사용)
    $article.dataset.postId = id;

    // 제목(26자 제한)
    $article.querySelector(".post-title").textContent = truncate(title, 26);

    // 시간
    const $time = $article.querySelector(".post-time");
    $time.setAttribute("datetime", modified_at ?? "");
    $time.textContent = formatDateTime(modified_at);

    // 메타(좋아요/댓글/조회수)
    $article.querySelector(".post-like").textContent = `좋아요 ${formatCount(likeCount)}`;
    $article.querySelector(".post-comment").textContent = `댓글 ${formatCount(commentCount)}`;
    $article.querySelector(".post-view").textContent = `조회수 ${formatCount(viewCount)}`;

    // 작성자
    $article.querySelector(".author-name").textContent = author ?? "";
    const $avatar = $article.querySelector(".author-avatar");
    if ($avatar) {
        // ensure utility classes are present (in case of older HTML)
        $avatar.classList.add("avatar", "avatar--sm");
        setAvatar($avatar, authorProfileImageUrl, author);
    }

    return $article;
}

// 리스트에 한 번만 이벤트를 달아서 카드 클릭/키보드 이벤트를 처리
function attachEventToList() {
    if (!$list) return;

    const goDetail = (postId) => {
        if (!postId) return;
        window.location.href = `postDetail.html?id=${postId}`;
    };

    $list.addEventListener("click", (e) => {
        const card = e.target.closest("[data-post-id]");
        if (!card || !$list.contains(card)) return;

        const {postId} = card.dataset;
        goDetail(postId);
    });

    $list.addEventListener("keydown", (e) => {
        if (e.key !== "Enter" && e.key !== " ") return;

        const card = e.target.closest("[data-post-id]");
        if (!card || !$list.contains(card)) return;

        e.preventDefault();
        const {postId} = card.dataset;
        goDetail(postId);
    });
}

async function loadMore() {
    if (isLoading || !hasNext) return;
    isLoading = true;
    $loader.style.display = "flex";

    try {
        const data = await fetchPosts({after: cursor, limit: PAGE_SIZE});
        const posts = data?.posts ?? [];
        posts.forEach((p) => $list.appendChild(createPostCard(p)));

        cursor = data?.next_cursor ?? cursor;
        hasNext = Boolean(data?.has_next);
    } catch (e) {
        // 간단한 사용자 알림
        console.error(e);
    } finally {
        $loader.style.display = hasNext ? "flex" : "none";
        isLoading = false;
    }
}

function setupInfiniteScroll() {
    const io = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    loadMore();
                }
            });
        },
        {rootMargin: "50px"}
    );
    io.observe($sentinel);
}

// 초기화
(function init() {
    if (!$list || !$loader || !$sentinel || !$template) return;
    setupInfiniteScroll();
    loadMore(); // 첫 페이지 로드
    attachEventToList();
})();