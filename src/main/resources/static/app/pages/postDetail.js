// 상세 페이지 스크립트
import {
    fetchPostDetail,
    fetchCommentList,
    removePost,
    likePost,
    unlikePost,
    addComment,
    editComment,
    removeComment,
} from "../features/posts/api.js";
import {fetchMyEditInfo} from "../features/users/api.js";
import {setAvatar} from "../components/Avatar.js";

// URL 파라미터
const params = new URLSearchParams(location.search);
const postId = Number(params.get("id") || 0);

// 요소 참조 (HTML에 존재한다고 가정된 id/class)
const $back = document.querySelector(".app-back"); // '<' 버튼
const $title = document.querySelector(".post-title");
const $author = document.querySelector(".post-author")
const $time = document.querySelector(".post-time");
const $body = document.querySelector(".post-body");
// 상세 콘텐츠 컨테이너 (초기 hidden)
const $container = document.getElementById("postContainer");

const $likeBtn = document.getElementById("likeBtn");
const $editBtn = document.getElementById("postEditBtn");

// $editBtn.classList.add('hidden');
const $deleteBtn = document.getElementById("postDeleteBtn");

// 대표 이미지 엘리먼트
const $postImage = document.getElementById("postImage");
const $mediaPlaceholder = document.querySelector(".media-placeholder");


const $likesNum = document.getElementById("likeCount");
const $viewsNum = document.getElementById("viewCount");
const $commentsNum = document.getElementById("commentCount");

// 좋아요 클릭 가능한 영역(버튼 대체): likeCount가 속한 .stat 블록을 버튼처럼 사용
const $likeArea = $likesNum ? $likesNum.closest(".stat") : null;

const $commentTextarea = document.getElementById("comment");
const $commentSubmit = document.getElementById("commentSubmitBtn");
const $commentList = document.querySelector(".comment-list");

// 댓글 무한 스크롤을 위한 Sentinel (스크롤 감지 요소)
const $commentSentinel = document.createElement("div");
$commentSentinel.id = "comment-sentinel";
$commentSentinel.style.height = "20px";
$commentSentinel.style.margin = "10px 0";
// 댓글 리스트 바로 뒤에 삽입하기 위해 부모 요소에 append (DOM 로드 후 init에서 처리)

// 상태
let isLiked = false;
let isOwner = false;
let likeCount = 0;
let viewCount = 0;
let commentCount = 0;

const COMMENT_LIMIT = 10;

// 댓글 페이징 상태
let commentCursorId = null;
let commentCursorTime = null; // modifiedBefore 파라미터용
let commentHasNext = true;
let isCommentLoading = false;

// 현재 로그인한 사용자 닉네임 (비로그인 시 null)
let currentUserNickname = null;

let editTargetCommentId = null; // 댓글 수정 대상 id (null이면 작성 모드)

// 유틸
function kCount(n) {
    const num = Number(n || 0);
    if (num < 1000) return String(num);
    return `${Math.floor(num / 1000)}k`;
}

function fmt(iso) {
    return String(iso || "").replace("T", " ").slice(0, 19);
}

function lockScroll(lock) {
    document.documentElement.classList.toggle("modal-open", lock);
    document.body.classList.toggle("modal-open", lock);
}

function updateOwnerControls(ownerFlag) {
    const shouldShow = Boolean(ownerFlag);
    const toggleBtn = ($btn) => {
        if (!$btn) return;
        $btn.classList.toggle("hidden", !shouldShow);
        $btn.classList.toggle("show", shouldShow);
        $btn.hidden = !shouldShow;
    };

    toggleBtn($editBtn);
    toggleBtn($deleteBtn);
}

// 모달 공통 (공용 CSS 구조 사용)
function showConfirmModal({title = "확인", message = "계속하시겠어요?", onConfirm}) {
    lockScroll(true);

    // 컨테이너(배경)
    const $modal = document.createElement("div");
    $modal.className = "modal is-open";

    // 다이얼로그
    const $dialog = document.createElement("div");
    $dialog.className = "modal__dialog";
    $dialog.setAttribute("role", "dialog");
    $dialog.setAttribute("aria-modal", "true");

    // 제목
    const $h = document.createElement("h3");
    $h.className = "modal__title";
    $h.id = "modalTitle";
    $h.textContent = title;
    $dialog.setAttribute("aria-labelledby", "modalTitle");

    // 메시지
    const $p = document.createElement("p");
    $p.className = "modal__message";
    $p.textContent = message;

    // 액션 영역
    const $actions = document.createElement("div");
    $actions.className = "modal__actions";

    // 취소 버튼
    const $cancel = document.createElement("button");
    $cancel.type = "button";
    $cancel.className = "btn btn--modal btn--modal-cancel";
    $cancel.textContent = "취소";

    // 확인 버튼
    const $ok = document.createElement("button");
    $ok.type = "button";
    $ok.className = "btn btn--modal btn--modal-confirm";
    $ok.textContent = "확인";

    $actions.append($cancel, $ok);
    $dialog.append($h, $p, $actions);
    $modal.append($dialog);

    function cleanup() {
        $modal.remove();
        lockScroll(false);
    }

    // 배경 클릭 시 닫히지 않게: 배경에 아무 동작 없음, 다이얼로그에서 이벤트만 중단
    $modal.addEventListener("click", (e) => {
        // 배경 클릭은 무시
        e.stopPropagation();
    });
    $dialog.addEventListener("click", (e) => e.stopPropagation());

    $cancel.addEventListener("click", cleanup);
    $ok.addEventListener("click", async () => {
        try {
            await onConfirm?.();
        } finally {
            cleanup();
        }
    });

    // Esc로 닫기
    const onKey = (e) => {
        if (e.key === "Escape") {
            cleanup();
            window.removeEventListener("keydown", onKey);
        }
    };
    window.addEventListener("keydown", onKey);

    document.body.appendChild($modal);

    // 포커스 이동
    $ok.focus();
}

// 댓글 아이템 템플릿 생성 (없다면)
function ensureCommentTemplate() {
    let $tpl = document.getElementById("comment-item-template");
    if ($tpl) return $tpl;

    $tpl = document.createElement("template");
    $tpl.id = "comment-item-template";
    $tpl.innerHTML = `
    <article class="comment-item">
      <header class="comment-head">
        <div class="comment-author">
          <span class="author-avatar avatar avatar--md" aria-hidden="true"></span>
          <div class="meta">
            <strong class="name"></strong>
            <time class="time" datetime=""></time>
          </div>
        </div>
        <div class="comment-actions">
          <button type="button" class="btn btn--ghost btn-edit" hidden>수정</button>
          <button type="button" class="btn btn--ghost btn-remove" hidden>삭제</button>
        </div>
      </header>
      <div class="comment-body"></div>
    </article>
  `.trim();
    document.body.appendChild($tpl);
    return $tpl;
}

// 좋아요 토글 공통 핸들러(중복 실행 방지)
let likeInFlight = false;

// 서버 응답을 상태에 적용하고 화면을 갱신하는 헬퍼
function applyServerState(postData, commentData) {
    const post = postData?.post || {};
    // 댓글 데이터 처리는 loadMoreComments 흐름으로 통합하므로 여기서는 Post 정보만 업데이트
    // 단, 초기 로딩 시점의 댓글 개수 등은 반영

    const likedFlag = postData?.liked ?? false;
    const ownerFlag = postData?.owner ?? false;

    isLiked = Boolean(likedFlag);
    isOwner = Boolean(ownerFlag);
    likeCount = Number(post.likeCount ?? 0);
    viewCount = Number(post.viewCount ?? 0);
    // 전체 댓글 수는 Post 정보에 포함된 commentCount를 사용 (댓글 목록 slice의 길이가 아님)
    commentCount = Number(post.commentCount ?? 0);

    // 본문/이미지만 렌더 (카운트는 상태값 기준으로 renderCounts에서 처리)
    renderPost(post);
    renderCounts();
    
    updateOwnerControls(isOwner);

    if ($container) $container.hidden = false;
}

// 댓글 목록 초기화 및 첫 페이지 로드
async function refreshComments() {
    commentCursorId = null;
    commentCursorTime = null;
    commentHasNext = true;
    isCommentLoading = false;
    $commentList.innerHTML = ""; // 목록 비우기
    
    await loadMoreComments();
}

async function refreshPostState() {
    // 게시글 상세 정보 조회
    const postData = await fetchPostDetail(postId);
    
    // 댓글 목록은 별도 초기화 함수로 관리
    // postData만 적용
    applyServerState(postData, null); 

    // 댓글 새로고침 (첫 페이지 로드)
    await refreshComments();
}

async function loadCurrentUserNickname() {
    try {
        const me = await fetchMyEditInfo();
        currentUserNickname = me?.nickname || null;
    } catch (e) {
        // 비로그인 또는 조회 실패 시 null 유지
        currentUserNickname = null;
    }
}

async function toggleLike() {
    if (likeInFlight) return;
    likeInFlight = true;
    try {
        if (isLiked) {
            await unlikePost(postId);
        } else {
            await likePost(postId);
        }
        // 좋아요 토글 시에는 게시글 정보(좋아요 수)만 갱신하면 됨
        const postData = await fetchPostDetail(postId);
        applyServerState(postData, null);
    } catch (e) {
        console.error(e);
    } finally {
        likeInFlight = false;
    }
}

function renderCounts() {
    if ($likesNum) $likesNum.textContent = kCount(likeCount);
    if ($viewsNum) $viewsNum.textContent = kCount(viewCount);
    if ($commentsNum) $commentsNum.textContent = kCount(commentCount);

    // 좋아요 토글 상태를 UI에 반영
    const label = `좋아요 ${kCount(likeCount)}`;

    if ($likeBtn) {
        $likeBtn.classList.toggle("btn-like--on", isLiked);
        $likeBtn.classList.toggle("btn-like--off", !isLiked);
        $likeBtn.setAttribute("aria-pressed", String(isLiked));
        $likeBtn.setAttribute("aria-label", label);
        const $lbl = $likeBtn.querySelector(".like-label");
        if ($lbl) $lbl.textContent = label;
    }

    if ($likeArea) {
        $likeArea.setAttribute("role", "button");
        $likeArea.tabIndex = 0;
        $likeArea.setAttribute("aria-pressed", String(isLiked));
        $likeArea.setAttribute("aria-label", label);
        $likeArea.classList.toggle("btn-like--on", isLiked);
        $likeArea.classList.toggle("btn-like--off", !isLiked);
        $likeArea.style.cursor = "pointer";
    }
}


function renderPost(post) {
    if ($title) $title.textContent = post.title || "";
    if ($author) $author.textContent = post.author || "";
    if ($time) {
        $time.dateTime = post.modified_at || "";
        $time.textContent = fmt(post.modified_at);
    }
    if ($body) $body.textContent = post.content || "";

    // 대표 이미지 표시 (primary_image_url만)
    const imgUrl = post.primary_image_url;
    if ($postImage) {
        if (imgUrl) {
            $postImage.src = imgUrl;
            $postImage.alt = "게시글 이미지";
            $postImage.hidden = false;
            if ($mediaPlaceholder) $mediaPlaceholder.style.display = "none";
        } else {
            $postImage.hidden = true;
            if ($mediaPlaceholder) $mediaPlaceholder.style.display = "block";
        }
    }

    // 글 상단 작성자 아바타 설정
    const $postAuthorAvatar = document.querySelector(".post-meta .author-avatar");
    if ($postAuthorAvatar) {
        setAvatar($postAuthorAvatar, post.authorProfileImageUrl, post.author);
    }

    // 카운트는 상태를 기반으로 별도 렌더
    // renderCounts();
}

function renderComments(comments = [], currentUserName) {
    ensureCommentTemplate();
    const $tpl = document.getElementById("comment-item-template");

    // 주의: append 방식이므로 innerHTML 초기화는 refreshComments()에서 수행함.
    
    comments.forEach((c) => {
        const $node = $tpl.content.firstElementChild.cloneNode(true);
        $node.dataset.id = String(c.id);

        // 아바타 세팅
        const $avatar = $node.querySelector(".author-avatar");
        setAvatar($avatar, c.authorProfileImageUrl, c.author);

        $node.querySelector(".name").textContent = c.author || "";
        const $tm = $node.querySelector(".time");
        $tm.dateTime = c.modified_at || "";
        $tm.textContent = fmt(c.modified_at);
        $node.querySelector(".comment-body").textContent = c.content || "";

        const isMine = currentUserName && c.author === currentUserName;
        const $edit = $node.querySelector(".btn-edit");
        const $remove = $node.querySelector(".btn-remove");
        $edit.classList.add('hidden');
        $remove.classList.add('hidden');
        if (isMine) {
            $edit.classList.toggle('hidden');
            $remove.classList.toggle('hidden');

            $edit.addEventListener("click", () => startEditComment(c.id, c.content));
            $remove.addEventListener("click", () => {
                showConfirmModal({
                    title: "댓글을 삭제하시겠습니까?",
                    message: "삭제한 내용은 복구 할 수 없습니다.",
                    onConfirm: async () => {
                        await removeComment(postId, c.id);
                        await refreshPostState(); // 삭제 후 갱신
                    },
                });
            });
        }

        $commentList.appendChild($node);
    });
}

// 댓글 더 불러오기 (무한 스크롤)
async function loadMoreComments() {
    if (isCommentLoading || !commentHasNext) return;
    isCommentLoading = true;

    try {
        // API 파라미터 준비
        // CommentService는 modifiedBefore(String "yyyy-MM-dd HH:mm:ss")와 cursorId(Long)를 받음
        const params = {
            limit: COMMENT_LIMIT,
            cursorId: commentCursorId,
            modifiedBefore: commentCursorTime // null이면 서버가 최신값 처리
        };

        const data = await fetchCommentList(postId, params);
        const comments = data?.comments ?? [];
        
        // 화면에 추가
        renderComments(comments, currentUserNickname);

        // 다음 커서 업데이트
        commentHasNext = Boolean(data?.has_next);
        commentCursorId = data?.next_cursor_id ?? null;

        // 다음 modifiedBefore를 위해 마지막 댓글의 시간을 가져옴
        // 서버 포맷에 맞춰 "yyyy-MM-dd HH:mm:ss" 변환 필요
        if (comments.length > 0) {
            const lastComment = comments[comments.length - 1];
            // lastComment.modified_at은 "2024-12-04T10:00:00" 형태의 ISO string이라고 가정
            if (lastComment.modified_at) {
                commentCursorTime = String(lastComment.modified_at).replace("T", " ").slice(0, 19);
            }
        }

    } catch (e) {
        console.error("Failed to load comments:", e);
    } finally {
        isCommentLoading = false;
        
        // 로딩 후에도 남은 데이터가 있고, Sentinel이 화면에 보인다면 즉시 추가 로드 (화면이 큰 경우 대비)
        if (commentHasNext && $commentSentinel) {
            const rect = $commentSentinel.getBoundingClientRect();
            const vh = window.innerHeight || document.documentElement.clientHeight;
            if (rect.top <= vh + 50) {
                 await loadMoreComments();
            }
        }
    }
}

function setupCommentInfiniteScroll() {
    // Sentinel을 댓글 리스트 다음에 삽입
    if ($commentList && $commentList.parentNode) {
        $commentList.parentNode.insertBefore($commentSentinel, $commentList.nextSibling);
    }

    const io = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) return;
                if (!commentHasNext || isCommentLoading) return;
                loadMoreComments();
            });
        },
        {
            rootMargin: "100px", // 조금 미리 로드
            threshold: 0
        }
    );
    io.observe($commentSentinel);
}

function setSubmitEnabled(enabled) {
    $commentSubmit.disabled = !enabled;
    $commentSubmit.classList.toggle("btn-primary--enabled", enabled);
    $commentSubmit.classList.toggle("btn-primary--disabled", !enabled);
    $commentSubmit.textContent = editTargetCommentId ? "댓글 수정" : "댓글 등록";
}

function startEditComment(id, content) {
    editTargetCommentId = id;
    $commentTextarea.value = content || "";
    $commentTextarea.focus();
    $commentTextarea.selectionStart = $commentTextarea.selectionEnd = $commentTextarea.value.length;
    setSubmitEnabled(Boolean($commentTextarea.value.trim()));
}

// 이벤트 바인딩
$back?.addEventListener("click", (e) => {
    e.preventDefault();
    location.href = "postList.html";
});

$editBtn?.addEventListener("click", () => {
    location.href = `postEdit.html?id=${postId}`;
});

$deleteBtn?.addEventListener("click", () => {
    showConfirmModal({
        title: "게시글을 삭제하시겠습니까?",
        message: "삭제한 내용은 복구 할 수 없습니다.",
        onConfirm: async () => {
            await removePost(postId);
            location.replace("postList.html");
        },
    });
});

// 좋아요 트리거: 버튼이 우선, 없으면 통계 블록(.stat)
// 두 군데 모두에 바인딩하지 않도록 분기
if ($likeBtn) {
    $likeBtn.addEventListener("click", toggleLike);
} else if ($likeArea) {
    $likeArea.addEventListener("click", toggleLike);
    $likeArea.addEventListener("keydown", (e) => {
        const key = e.key || e.code;
        if (key === "Enter" || key === " " || key === "Spacebar" || key === "Space") {
            e.preventDefault();
            toggleLike();
        }
    });
}

$commentTextarea?.addEventListener("input", () => {
    const text = $commentTextarea.value.trim();
    setSubmitEnabled(Boolean(text));
});

$commentSubmit?.addEventListener("click", async () => {
    const text = ($commentTextarea.value || "").trim();
    if (!text) return;

    try {
        if (editTargetCommentId) {
            await editComment(postId, editTargetCommentId, text);
        } else {
            await addComment(postId, text);
        }
        // 입력창 초기화
        editTargetCommentId = null;
        $commentTextarea.value = "";
        setSubmitEnabled(false);

        await refreshPostState(); // 댓글 등록/수정 후 전체 갱신 (댓글 목록 리셋 포함)
    } catch (e) {
        console.error(e);
    }
});

// 초기 구동
async function boot() {
    if (!postId) return;

    try {
        await loadCurrentUserNickname();
        // 무한 스크롤 설정
        setupCommentInfiniteScroll();
        
        await refreshPostState();
    } catch (e) {
        console.error(e);
    }
}

// 최초 상태
if ($commentSubmit) setSubmitEnabled(false);
await boot();
