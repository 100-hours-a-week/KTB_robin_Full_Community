import {
    deleteProfileImage,
    fetchMyEditInfo,
    updateNickname,
    uploadProfileImage,
    withdraw
} from "../features/users/api.js";
import {isNicknameAvailable} from "../features/availability/api.js";
import {logout} from "../features/auth/api.js";
import {footerToast} from "../components/Toast.js";

const $email = document.getElementById("email");
const $nickname = document.getElementById("nickname");
const $helpNickname = document.getElementById("helpNickname");
const $btnUpdate = document.getElementById("btnUpdateProfile");
const $btnLeave = document.getElementById("btnLeave");
const $file = document.getElementById("profileImage");
const $avatarTrigger = document.querySelector("label.avatar-trigger");
const $avatar = document.getElementById("avatar-trigger");

let originalNickname = "";
let debounceTimer = null;
let validating = false;

function setUpdateEnabled(enabled) {
    if (!$btnUpdate) return;
    $btnUpdate.disabled = !enabled;
    $btnUpdate.classList.toggle("btn:disabled", enabled);
    $btnUpdate.classList.toggle("btn:disabled", !enabled);
}

function setNicknameHelper({show = false, text = "", error = false} = {}) {
    if (!$helpNickname) return;
    if (!show) {
        $helpNickname.textContent = "";
        $helpNickname.hidden = true;
        $helpNickname.classList.remove("helper--error");
        return;
    }
    $helpNickname.hidden = false;
    $helpNickname.textContent = text || "";
    $helpNickname.classList.toggle("helper--error", Boolean(error));
}

function lockScroll(lock) {
    document.documentElement.classList.toggle("modal-open", lock);
    document.body.classList.toggle("modal-open", lock);
}

function showConfirmModal({title = "확인", message = "계속하시겠어요?", confirmText = "확인", cancelText = "취소", onConfirm}) {
    lockScroll(true);
    const $modal = document.createElement("div");
    $modal.className = "modal is-open";

    const $dialog = document.createElement("div");
    $dialog.className = "modal__dialog";
    $dialog.setAttribute("role", "dialog");
    $dialog.setAttribute("aria-modal", "true");

    const $h = document.createElement("h3");
    $h.className = "modal__title";
    $h.id = "modalTitle";
    $h.textContent = title;
    $dialog.setAttribute("aria-labelledby", "modalTitle");

    const $p = document.createElement("p");
    $p.className = "modal__message";
    $p.textContent = message;

    const $actions = document.createElement("div");
    $actions.className = "modal__actions";

    const $cancel = document.createElement("button");
    $cancel.type = "button";
    $cancel.className = "btn btn--modal btn--modal-cancel";
    $cancel.textContent = cancelText;

    const $ok = document.createElement("button");
    $ok.type = "button";
    $ok.className = "btn btn--modal btn--modal-confirm";
    $ok.textContent = confirmText;

    $actions.append($cancel, $ok);
    $dialog.append($h, $p, $actions);
    $modal.append($dialog);

    function cleanup() {
        $modal.remove();
        lockScroll(false);
    }

    $modal.addEventListener("click", (e) => e.stopPropagation());
    $dialog.addEventListener("click", (e) => e.stopPropagation());
    $cancel.addEventListener("click", cleanup);
    $ok.addEventListener("click", async () => {
        try {
            await onConfirm?.();
        } finally {
            cleanup();
        }
    });
    const onKey = (e) => {
        if (e.key === "Escape") {
            cleanup();
            window.removeEventListener("keydown", onKey);
        }
    };
    window.addEventListener("keydown", onKey);
    document.body.appendChild($modal);
    $ok.focus();
}

function hideFooterToast() {
    const $footerToast = document.getElementById("toast");
    if ($footerToast) {
        $footerToast.hidden = true;
    }
}

function preloadStoredAvatar() {
    if (!$avatar) return;
    try {
        const storedUrl = localStorage.getItem("profile_image_url");
        if (!storedUrl || typeof storedUrl !== "string") return;
        const url = storedUrl.trim();
        if (!url) return;

        $avatar.style.backgroundImage = `url("${encodeURI(url)}")`;
        $avatar.style.backgroundSize = "cover";
        $avatar.style.backgroundPosition = "center";
        $avatar.style.backgroundRepeat = "no-repeat";
        $avatar.style.opacity = 0.5;
        $avatar.setAttribute("aria-hidden", "true");
    } catch (_) {
    }
}

function setEmailValue(emailVal) {
    if (!$email) return;
    if ($email.tagName === "INPUT") {
        $email.value = emailVal;
        $email.setAttribute("readonly", "true");
        $email.setAttribute("aria-readonly", "true");
        $email.tabIndex = -1;
        $email.classList.add("input--readonly");
    } else {
        $email.textContent = emailVal;
    }
}

function setNicknameValue(nickname) {
    originalNickname = nickname;
    if ($nickname) $nickname.value = nickname;
}

function resetNicknameState() {
    setNicknameHelper({show: false});
    setUpdateEnabled(false);
}

function bindEvents() {
    $file?.addEventListener("change", async () => {
        const file = $file.files && $file.files[0];
        if (!file) return;
        try {
            // 기존 이미지가 있으면 먼저 삭제 호출 -> 그 다음에 새 이미지 등록
            try {
                const existing = localStorage.getItem("profile_image_url");
                if (existing) {
                    await deleteProfileImage();
                    // 삭제 후 클라이언트 캐시/저장값 제거
                    try {
                        localStorage.removeItem("profile_image_url");
                    } catch (_) {
                    }
                }
            } catch (delErr) {
                // 삭제 실패여도 새 이미지 업로드를 시도하도록 경고만 남김
                console.warn("기존 프로필 이미지 삭제 실패:", delErr);
            }

            const res = await uploadProfileImage(file); // { image_url }
            const url = res?.profileImageUrl || res?.image_url || "";
            if ($avatarTrigger) {
                $avatarTrigger.style.backgroundImage = url ? `url("${encodeURI(url)}")` : "";
                $avatarTrigger.setAttribute("aria-hidden", url ? "true" : "false");
                // 저장 성공 시 로컬 스토리지에 반영하여 다른 페이지(헤더 등)에서 즉시 보이도록 함
                try {
                    if (url) localStorage.setItem("profile_image_url", url);
                    else localStorage.removeItem("profile_image_url");
                    try {
                        window.dispatchEvent(new CustomEvent("profile-image-changed", {detail: {url}}));
                    } catch (_) {
                    }
                } catch (_) {
                }
            }
        } catch (e) {
            console.error(e);
            setNicknameHelper({show: true, text: "프로필 이미지 업로드에 실패했습니다.", error: true});
        } finally {
            $file.value = "";
        }
    });

    $nickname?.addEventListener("input", () => {
        const value = ($nickname.value || "").trim();

        // 기존 닉네임과 동일한 값을 입력했을때는 중복검사에서 제외한다.
        if (value === originalNickname) {
            setNicknameHelper({show: false});
            setUpdateEnabled(false);
            return;
        }

        if (!value) {
            setNicknameHelper({show: false});
            setUpdateEnabled(false);
            return;
        }

        if (debounceTimer) window.clearTimeout(debounceTimer);
        debounceTimer = window.setTimeout(validateNickname, 350);
    });

    $btnUpdate?.addEventListener("click", async () => {
        const next = ($nickname.value || "").trim();
        if (!next || next === originalNickname) return;
        try {
            const res = await updateNickname(next);
            originalNickname = res?.newNickname || next;

            $nickname.value = originalNickname;
            setNicknameHelper({show: false});
            setUpdateEnabled(false);

            footerToast();

        } catch (e) {
            console.error(e);
            setNicknameHelper({show: true, text: "닉네임 수정에 실패했습니다.", error: true});
        }
    });

    // Withdraw
    $btnLeave?.addEventListener("click", () => {
        showConfirmModal({
            title: "정말 탈퇴하시겠습니까?",
            message: "삭제한 정보는 복구할 수 없습니다.",
            confirmText: "탈퇴",
            cancelText: "취소",
            onConfirm: async () => {
                try {
                    await withdraw();
                    logout();
                    location.replace("login.html");
                } catch (e) {
                    console.error(e);
                    setNicknameHelper({show: true, text: "탈퇴 처리에 실패했습니다.", error: true});
                }
            },
        });
    });
}

async function validateNickname() {
    const value = ($nickname?.value || "").trim();
    if (!value || value === originalNickname) {
        setNicknameHelper({show: false});
        setUpdateEnabled(false);
        return;
    }
    if (validating) return; // avoid overlap
    validating = true;
    try {
        const ok = await isNicknameAvailable(value);
        if (ok) {
            setNicknameHelper({show: true, text: "사용 가능한 닉네임입니다.", error: false});
            setUpdateEnabled(true);
        } else {
            setNicknameHelper({show: true, text: "이미 사용 중인 닉네임입니다.", error: true});
            setUpdateEnabled(false);
        }
    } catch (e) {
        console.error(e);
        setNicknameHelper({show: true, text: "닉네임 확인 중 오류가 발생했습니다.", error: true});
        setUpdateEnabled(false);
    } finally {
        validating = false;
    }
}

async function loadUserInfo() {
    const me = await fetchMyEditInfo();
    setEmailValue(me?.email || "");
    setNicknameValue(me?.nickname || "");
    resetNicknameState();
}

async function boot() {
    hideFooterToast();
    preloadStoredAvatar();
    try {
        await loadUserInfo();
    } catch (e) {
        console.error(e);
    }
    bindEvents();
}

boot();
