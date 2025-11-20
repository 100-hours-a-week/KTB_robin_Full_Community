// 게시글 작성/수정 페이지 공통 폼 유틸
const TITLE_MAX = 26; // 한글 기준 26글자
const FILE_PLACEHOLDER = "파일을 선택해주세요.";

function strLen(s) {
    return Array.from(String(s || "")).length;
}

function enforceTitleLimit($title) {
    if (!$title) return;
    const chars = Array.from(String($title.value || ""));
    if (chars.length > TITLE_MAX) {
        $title.value = chars.slice(0, TITLE_MAX).join("");
    }
}

function setHelper($helper, msg) {
    if (!$helper) return;
    if (!msg) {
        $helper.textContent = "";
        $helper.style.display = "none";
    } else {
        $helper.textContent = msg;
        $helper.style.display = "block";
    }
}

function setSubmitEnabled($submit, enabled) {
    if ($submit) {
        $submit.disabled = !enabled;
        $submit.classList.toggle("btn-primary--disabled", !enabled);
    }
}

function validatePostForm({$title, $content, $titleHelper, $bodyHelper, $submit}) {
    const title = $title?.value?.trim() || "";
    const body = $content?.value?.trim() || "";

    const titleErrors = [];
    const bodyErrors = [];

    if (strLen(title) > TITLE_MAX) {
        titleErrors.push(`제목은 최대 ${TITLE_MAX}자까지 입력할 수 있어요.`);
    }
    if (title.length === 0) {
        titleErrors.push("제목을 비울 수 없어요.");
    }
    if (body.length === 0) {
        bodyErrors.push("내용은 비울 수 없어요.");
    }

    setHelper($titleHelper, titleErrors.join(" \n"));
    setHelper($bodyHelper, bodyErrors.join(" \n"));
    const ok = titleErrors.length === 0 && bodyErrors.length === 0;
    setSubmitEnabled($submit, ok);
    return ok;
}

function selectPostFormElements() {
    const $form = document.querySelector(".edit-form");
    const $title = document.getElementById("title");
    const $content = document.getElementById("content");
    const $image = document.getElementById("image");
    const $fileMsg = document.querySelector(".file-message");
    const $titleHelper = document.querySelector(".editor-title-helper");
    const $bodyHelper = document.querySelector(".editor-body-helper");
    const $submit = $form?.querySelector('button[type="submit"]');

    return {$form, $title, $content, $image, $fileMsg, $titleHelper, $bodyHelper, $submit};
}

function createPostFormContext() {
    const elements = selectPostFormElements();
    const {$title, $titleHelper, $bodyHelper, $submit} = elements;

    return {
        ...elements,
        enforceTitleLimit: () => enforceTitleLimit($title),
        setTitleHelper: (msg) => setHelper($titleHelper, msg),
        setBodyHelper: (msg) => setHelper($bodyHelper, msg),
        setSubmitEnabled: (enabled) => setSubmitEnabled($submit, enabled),
        validate: () => validatePostForm(elements),
    };
}

export {
    FILE_PLACEHOLDER,
    TITLE_MAX,
    createPostFormContext,
    enforceTitleLimit,
    selectPostFormElements,
    setSubmitEnabled,
};
