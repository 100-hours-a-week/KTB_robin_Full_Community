// 게시글 수정 페이지 스크립트
import {fetchPostDetail, editPost} from "../features/posts/api.js";
import {createPostFormContext, FILE_PLACEHOLDER} from "./postFormCommon.js";

// URL 파라미터에서 post id 추출
const params = new URLSearchParams(location.search);
const postId = Number(params.get("id") || 0);

// 요소 참조
const $back = document.querySelector(".app-back");
const {
    $form,
    $title,
    $content,
    $image,
    $fileMsg,
    $titleHelper,
    $bodyHelper,
    $submit,
    enforceTitleLimit,
    setTitleHelper,
    setBodyHelper,
    setSubmitEnabled,
    validate,
} = createPostFormContext();

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
