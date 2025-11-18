// 게시글 수정 페이지 스크립트
import {fetchPostDetail, editPost} from "../features/posts/api.js";

// URL 파라미터에서 post id 추출
const params = new URLSearchParams(location.search);
const postId = Number(params.get("id") || 0);

// 요소 참조
const $back = document.querySelector(".app-back");
const $form = document.querySelector(".edit-form");
const $title = document.getElementById("title");
const $content = document.getElementById("content");
const $image = document.getElementById("image");
const $fileMsg = document.querySelector(".file-message");
const $titleHelper = document.querySelector(".editor-title-helper");
const $bodyHelper = document.querySelector(".editor-body-helper");
const $submit = $form?.querySelector('button[type="submit"]');

// 상수
const TITLE_MAX = 26; // 한글 기준 26글자
const FILE_PLACEHOLDER = "파일을 선택해주세요.";

// 유틸: 문자열 길이 (grapheme-기반에 가깝게)
function strLen(s) {
    return Array.from(String(s || "")).length;
}

// 제목 입력을 26글자로 강제 제한 (한글 등 결합 문자 고려)
function enforceTitleLimit() {
    if (!$title) return;
    const chars = Array.from(String($title.value || ""));
    if (chars.length > TITLE_MAX) {
        $title.value = chars.slice(0, TITLE_MAX).join("");
    }
}

function setTitleHelper(msg) {
    if (!$titleHelper) return;
    if (!msg) {
        $titleHelper.textContent = "";
        $titleHelper.style.display = "none";
    } else {
        $titleHelper.textContent = msg;
        $titleHelper.style.display = "block";
    }
}

function setBodyHelper(msg) {
    if (!$bodyHelper) return;
    if (!msg) {
        $bodyHelper.textContent = "";
        $bodyHelper.style.display = "none";
    } else {
        $bodyHelper.textContent = msg;
        $bodyHelper.style.display = "block";
    }
}

function setSubmitEnabled(enabled) {
    if ($submit) {
        $submit.disabled = !enabled;
        $submit.classList.toggle("btn-primary--disabled", !enabled);
    }
}

function validate() {
    const title = $title?.value?.trim() || "";
    const body = $content?.value?.trim() || "";

    const titleErrors = [];
    const bodyErrors = [];

    if (strLen(title) > TITLE_MAX) {
        titleErrors.push(`제목은 최대 ${TITLE_MAX}자까지 입력할 수 있어요.`);
    }
    if (title.length === 0) {
        titleErrors.push(`제목을 비울 수 없어요.`);
    }
    if (body.length === 0) {
        bodyErrors.push("내용은 비울 수 없어요.");
    }

    setTitleHelper(titleErrors.join(" \n"));
    setBodyHelper(bodyErrors.join(" \n"));
    const ok = titleErrors.length === 0 && bodyErrors.length === 0;
    setSubmitEnabled(ok);
    return ok;
}

function initBackLink() {
    if ($back) {
        const url = postId ? `postDetail.html?id=${postId}` : "postDetail.html";
        $back.setAttribute("href", url);
    }
}

function bindEvents() {
    if ($title) {
        $title.addEventListener("input", () => {
            enforceTitleLimit();
            validate();
        });
    }
    if ($content) {
        $content.addEventListener("input", validate);
    }
    if ($image && $fileMsg) {
        $image.addEventListener("change", () => {
            const file = $image.files && $image.files[0];
            $fileMsg.textContent = file ? file.name : FILE_PLACEHOLDER;
        });
    }

    if ($form) {
        $form.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!validate()) return;
            try {
                setSubmitEnabled(false);
                const dto = {
                    title: ($title?.value || "").trim(),
                    content: ($content?.value || "").trim(),
                };
                const imageFile = $image?.files?.[0] || null;
                await editPost(postId, dto, imageFile);
                // 수정 완료 후 상세로 이동
                location.href = `postDetail.html?id=${postId}`;
            } catch (e) {
                console.error(e);
                // 실패 시 다시 버튼 활성화
                setSubmitEnabled(true);
                setBodyHelper("수정 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.");
            }
        });
    }
}

async function prefill() {
    if (!postId) return;
    try {
        const data = await fetchPostDetail(postId);
        const post = data?.post || {};

        if ($title) $title.value = post.title || "";
        // 서버에서 온 제목이 길이를 초과할 수도 있으므로 한 번 더 클램프
        enforceTitleLimit();
        if ($content) $content.value = post.content || "";

        // 기존 첨부 파일명이 있다면 파일명 영역에 반영
        const url = post.primary_image_url || "";
        if ($fileMsg) {
            if (url) {
                try {
                    const path = new URL(url, location.origin).pathname;
                    const name = decodeURIComponent(path.split("/").pop() || "");
                    $fileMsg.textContent = name || FILE_PLACEHOLDER;
                } catch {
                    // url 파싱 실패 시 단순 분할 시도
                    const name = decodeURIComponent(String(url).split("/").pop() || "");
                    $fileMsg.textContent = name || FILE_PLACEHOLDER;
                }
            } else {
                $fileMsg.textContent = FILE_PLACEHOLDER;
            }
        }
    } catch (e) {
        console.error(e);
    } finally {
        // 초기 검증 상태 반영
        validate();
    }
}

// 부트스트랩
(async function boot() {
    initBackLink();
    bindEvents();
    setTitleHelper("");
    setBodyHelper(""); // 기본은 숨김
    await prefill();
})();
