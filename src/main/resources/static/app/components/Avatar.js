export function setAvatar($el, url, name) {
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