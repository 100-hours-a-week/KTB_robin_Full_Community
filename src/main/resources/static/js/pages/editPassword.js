import {updatePassword} from "../features/users/api.js";

// DOM refs
const $pw = document.getElementById("password");
const $pw2 = document.getElementById("passwordConfirm");
const $helpPw = document.getElementById("helpPassword");
const $helpPw2 = document.getElementById("helpPasswordConfirm");
const $btn = document.getElementById("btnSubmitEditPassword");
// toast handled by core/toast.js via id="toast" in footer

// removed local toast; use shared toast helper

function setBtnEnabled(enabled) {
    if (!$btn) return;
    $btn.disabled = !enabled;
    $btn.classList.toggle("btn:disabled", enabled);
    $btn.classList.toggle("btn:disabled", !enabled);
}

function setHelper($el, text = "") {
    if (!$el) return;
    if (!text) {
        $el.textContent = "";
        $el.classList.add("is-hidden");
    } else {
        $el.textContent = text;
        $el.classList.remove("is-hidden");
    }
}

function isStrong(pw) {
    // 8-20, at least one upper, lower, digit, special
    const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,20}$/;
    return re.test(pw || "");
}

function validate() {
    const p1 = ($pw?.value || "").trim();
    const p2 = ($pw2?.value || "").trim();

    // Reset helpers first
    setHelper($helpPw, "");
    setHelper($helpPw2, "");

    // Empty states
    if (!p1) {
        setHelper($helpPw, "비밀번호를 입력해주세요.");
    } else if (!isStrong(p1)) {
        // Strength guidance (not specified but helpful)
        setHelper(
            $helpPw,
            "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
        );
    }

    if (!p2) {
        setHelper($helpPw2, "비밀번호를 한 번 더 입력해주세요.");
    }

    // Mismatch messages if both present
    if (p1 && p2 && p1 !== p2) {
        setHelper($helpPw, "비밀번호 확인과 다릅니다.");
        setHelper($helpPw2, "비밀번호와 다릅니다.");
    }

    const ok = Boolean(p1 && p2 && p1 === p2 && isStrong(p1));
    setBtnEnabled(ok);
    return ok;
}

function bindEvents() {
    $pw?.addEventListener("input", validate);
    $pw2?.addEventListener("input", validate);

    $btn?.addEventListener("click", async () => {
        const ok = validate();
        if (!ok) return;
        const newPassword = ($pw?.value || "").trim();
        try {
            await updatePassword(newPassword);

            (function localFooterToast(msg = "완료", duration = 2000) {
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
                el.style.background = "#b79ef0"; // 스크린샷 유사 색
                el.style.color = "#fff";
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
            })("수정완료", 1800);

            // 수정 완료했다면 입력폼을 비운다
            if ($pw) $pw.value = "";
            if ($pw2) $pw2.value = "";
            setHelper($helpPw, "");
            setHelper($helpPw2, "");
            setBtnEnabled(false);
        } catch (e) {
            console.error(e);
            setHelper($helpPw, "비밀번호 수정에 실패했습니다. 다시 시도해주세요.");
        }
    });
}

// init
setBtnEnabled(false);
bindEvents();
validate();
