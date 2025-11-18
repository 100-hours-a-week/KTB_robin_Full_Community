import {updatePassword} from "../features/users/api.js";
import {footerToast} from "../../app/components/Toast.js";

// DOM refs
const $pw = document.getElementById("password");
const $pw2 = document.getElementById("passwordConfirm");
const $helpPw = document.getElementById("helpPassword");
const $helpPw2 = document.getElementById("helpPasswordConfirm");
const $btn = document.getElementById("btnSubmitEditPassword");

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

            footerToast();

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
