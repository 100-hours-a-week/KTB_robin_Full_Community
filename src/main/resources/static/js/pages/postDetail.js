// 상세 페이지 스크립트
import {
    fetchPostDetail,
    removePost,
    likePost,
    unlikePost,
    addComment,
    editComment,
    removeComment,
} from "../features/posts/api.js";
import { fetchMyEditInfo } from "../features/users/api.js";

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

// 상태
let isLiked = false;
let isOwner = false;
let likeCount = 0;
let viewCount = 0;
let commentCount = 0;

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

// 아바타 설정 유틸: background-image로 세팅, 없으면 이니셜 폴백
function setAvatar($el, url, name) {
    if (!$el) return;
    const safeUrl = url ? encodeURI(url) : null;

    // 배경 초기화
    $el.style.removeProperty("background-image");
    $el.textContent = "";

    if (safeUrl) {
        // 따옴표로 감싸 특수문자 안전성 확보
        $el.style.backgroundImage = `url("${safeUrl}")`;
        $el.setAttribute("aria-hidden", "true");
    } else {
        // 이미지가 없으면 이니셜 폴백
        const initial = (name || "?").trim().charAt(0);
        $el.textContent = initial;
        $el.setAttribute("aria-hidden", "false");
        $el.title = name || "";
    }
}

// 모달 공통 (공용 CSS 구조 사용)
function showConfirmModal({ title = "확인", message = "계속하시겠어요?", onConfirm }) {
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
          </div>
          <time class="time" datetime=""></time>
        </div>
        <div class="comment-actions">
          <button type="button" class="btn--ghost btn-edit" hidden>수정</button>
          <button type="button" class="btn--ghost btn-remove" hidden>삭제</button>
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
function applyServerState(data) {
    const { post, is_liked, is_owner, comments } = data || {};
    isLiked = Boolean(is_liked);
    isOwner = Boolean(is_owner);
    likeCount = Number(post?.likeCount ?? 0);
    viewCount = Number(post?.viewCount ?? 0);
    commentCount = (post?.comments ?? comments?.length ?? 0);

    // 본문/이미지만 렌더 (카운트는 상태값 기준으로 renderCounts에서 처리)
    renderPost(post || {});
    renderCounts();
    // 댓글의 수정/삭제 버튼 노출은 '현재 로그인 유저' 기준으로 판단해야 함
    renderComments(comments || [], currentUserNickname);

    if ($editBtn) $editBtn.hidden = !isOwner;
    if ($deleteBtn) $deleteBtn.hidden = !isOwner;

    if ($container) $container.hidden = false;
}

async function refreshPostState() {
    const data = await fetchPostDetail(postId);
    applyServerState(data);
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
        // 서버에서 정확한 최신 상태로 재동기화
        await refreshPostState();
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
    const $postAuthorAvatar = document.querySelector(".post-head .author-avatar");
    if ($postAuthorAvatar) {
        setAvatar($postAuthorAvatar, post.authorProfileImageUrl, post.author);
    }

    // 카운트는 상태를 기반으로 별도 렌더
    // renderCounts();
}

function renderComments(comments = [], currentUserName) {
    ensureCommentTemplate();
    const $tpl = document.getElementById("comment-item-template");

    $commentList.innerHTML = ""; // 초기화
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
        if (isMine) {
            $edit.hidden = false;
            $remove.hidden = false;

            $edit.addEventListener("click", () => startEditComment(c.id, c.content));
            $remove.addEventListener("click", () => {
                showConfirmModal({
                    title: "댓글을 삭제하시겠습니까?",
                    message: "삭제한 내용은 복구 할 수 없습니다.",
                    onConfirm: async () => {
                        await removeComment(postId, c.id);
                        await boot(); // 새로고침 대신 재로드
                    },
                });
            });
        }

        $commentList.appendChild($node);
    });
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

        await boot(); // 최신 상태 재로딩
    } catch (e) {
        console.error(e);
    }
});

// 초기 구동
async function boot() {
    if (!postId) return;

    try {
        // 현재 로그인 사용자 닉네임을 먼저 로드하여 댓글 UI 소유권 판단에 사용
        await loadCurrentUserNickname();

        const data = await fetchPostDetail(postId);
        const { post, is_liked, is_owner, comments } = data || {};

        // 서버 값 그대로 반영 (초기 로드시 수치 보정/증감 금지)
        isLiked = Boolean(is_liked);
        isOwner = Boolean(is_owner);

        // 수치 필드도 서버 값을 신뢰
        likeCount = Number(post?.likeCount ?? 0);
        viewCount = Number(post?.viewCount ?? 0);
        commentCount = (post?.comments ?? comments?.length ?? 0);

        renderPost(post || {}); // 내부에서 like/view/comment를 덮어쓰지 않게 주의
        renderCounts();
        // 댓글의 수정/삭제 버튼 노출은 로그인 유저 기준
        renderComments(comments || [], currentUserNickname);

        if ($editBtn) $editBtn.hidden = !isOwner;
        if ($deleteBtn) $deleteBtn.hidden = !isOwner;

        if ($container) $container.hidden = false;
    } catch (e) {
        console.error(e);
    }
}

// 최초 상태
if ($commentSubmit) setSubmitEnabled(false);
boot();