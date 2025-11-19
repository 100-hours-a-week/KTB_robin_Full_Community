export const footerToast = (function() {
    function localFooterToast(msg = "수정완료", duration = 2000) {
        const id = "local-edit-password-toast";
        // 이미 있으면 제거 후 재생성
        const el = document.createElement("div");
        el.id = id;
        el.textContent = msg;
        // 간단한 스타일; 필요하면 CSS로 이동 가능
        el.style.position = "fixed";
        el.style.left = "50%";
        el.style.bottom = "28px";
        el.style.transform = "translateX(-50%)";
        el.style.background = "#fff"; // 스크린샷 유사 색
        el.style.color = "#050505";
        el.style.padding = "10px 28px";
        el.style.borderRadius = "999px";
        el.style.boxShadow = "0 4px 12px rgba(0,0,0,0.12)";
        el.style.zIndex = "1100";
        el.style.fontSize = "20px";
        el.style.opacity = "0";
        el.style.transition = "opacity 180ms ease, transform 180ms ease";
        document.body.appendChild(el);

        // 트리거 애니메이션
        requestAnimationFrame(() => {
            el.style.opacity = "1";
            el.style.transform = "translateX(-50%) translateY(-6px)";
        });

        setTimeout(() => {
            el.style.opacity = "0";
            el.style.transform = "translateX(-50%) translateY(0)";
            setTimeout(() => el.remove(), 220);
        }, duration);
    }
    return localFooterToast;
})();