// 회원가입
import {isValidEmail, isValidPassword} from "../features/auth/validator.js";
import {isEmailAvailable, isNicknameAvailable} from "../features/availability/api.js";
import {apiForm} from "../core/apiClient.js";
import {formWithJson, appendFile} from "../core/form.js";

// 엘리먼트
const avatarInput = document.getElementById("avatar");
const avatarTrigger = document.getElementById("avatarTrigger");
const emailEl = document.getElementById("email");
const passwordEl = document.getElementById("password");
const passwordConfirmEl = document.getElementById("passwordConfirm");
const nicknameEl = document.getElementById("nickname");
const formEl = document.querySelector("form");
const submitBtn = document.getElementById("signupSubmitBtn");

// 각 필드 하단의 helper 요소(첫 번째 helper 사용)
const emailHelper = emailEl?.closest(".form-group")?.querySelector(".helper");
const passwordHelper = passwordEl?.closest(".form-group")?.querySelector(".helper");
const passwordConfirmHelper = passwordConfirmEl?.closest(".form-group")?.querySelector(".helper");
const nicknameHelper = nicknameEl?.closest(".form-group")?.querySelector(".helper");
const avatarLabelHelper = document.querySelector(".avatar-label .helper:last-of-type");

// 메시지
const MSG = {
    avatarRequired: "*프로필 사진을 추가해주세요.",
    emailEmpty: "*이메일을 입력해주세요.",
    emailInvalid: "*올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)",
    emailExisting: "*중복된 이메일입니다.",
    passwordInvalid: "*비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다.",
    passwordEmpty: "*비밀번호를 입력해주세요",
    passwordConfirmMismatch: "*비밀번호가 다릅니다.",
    passwordConfirmEmpty: "*비밀번호를 한번더 입력해주세요",
    nicknameEmpty: "*닉네임을 입력해주세요.",
    nicknameHasBlank: "*띄어쓰기를 없애주세요",
    nicknameExisting: "*중복된 닉네임 입니다.",
    nicknameTooLong: "*닉네임은 최대 10자까지 작성 가능합니다.",
    signupFailed: "*회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.",
};

// 최초 로딩과 사용자 입력(터치)을 구분하기 위한 플래그
let emailTouched = false;
let nicknameTouched = false;

// 로딩 시점 안내 메시지
if (emailHelper) showHelper(emailHelper, MSG.emailEmpty);

if (passwordHelper) showHelper(passwordHelper, MSG.passwordInvalid);
if (passwordConfirmHelper) showHelper(passwordConfirmHelper, MSG.passwordConfirmEmpty);
if (nicknameHelper) showHelper(nicknameHelper, MSG.nicknameEmpty);
if (avatarLabelHelper) showHelper(avatarLabelHelper, MSG.avatarRequired);

// 상태 캐시(버튼 활성화 판단용)
const state = {
    emailOk: false,
    passwordOk: false,
    passwordConfirmOk: false,
    nicknameOk: false,
    avatarOk: false,
};

// 디바운스 헬퍼
function debounce(fn, wait = 400) {
    let t;
    return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), wait);
    };
}

// Helper 토글
function showHelper(el, msg) {
    if (!el) return;
    el.textContent = msg || "";
    el.classList.remove("is-hidden");
    el.classList.add("is-visible");
}

function hideHelper(el) {
    if (!el) return;
    el.textContent = "";
    el.classList.remove("is-visible");
    el.classList.add("is-hidden");
}

// 버튼 활성화 제어
function updateSubmitState() {
    const enabled = state.emailOk && state.passwordOk && state.passwordConfirmOk && state.nicknameOk && state.avatarOk;
    submitBtn.disabled = !enabled;
}

// 유효성 검사
async function validateEmail() {
    const v = emailEl.value.trim();
    // 방어: 호출 시점에 값이 빈 경우
    if (!v) {
        showHelper(emailHelper, MSG.emailEmpty);
        state.emailOk = false;
        updateSubmitState();
        return false;
    }
    try {
        const ok = await isEmailAvailable(v);
        if (!ok) {
            showHelper(emailHelper, MSG.emailExisting);
            state.emailOk = false;
            updateSubmitState();
            return false;
        }
        hideHelper(emailHelper);
        state.emailOk = true;
        updateSubmitState();
        return true;
    } catch {
        showHelper(emailHelper, MSG.emailExisting);
        state.emailOk = false;
        updateSubmitState();
        return false;
    }
}

function validatePasswordSync() {
    const v = passwordEl.value;
    const {ok, reason} = isValidPassword(v);
    if (ok) {
        hideHelper(passwordHelper);
        state.passwordOk = true;
        updateSubmitState();
        return true;
    }
    if (reason === "empty") showHelper(passwordHelper, MSG.passwordEmpty);
    else if (reason === "invalid") showHelper(passwordHelper, MSG.passwordInvalid);
    state.passwordOk = false;
    updateSubmitState();
    return false;
}

function validatePasswordConfirmSync() {
    const v = passwordConfirmEl.value;
    if (!v) {
        showHelper(passwordConfirmHelper, MSG.passwordConfirmEmpty);
        state.passwordConfirmOk = false;
        updateSubmitState();
        return false;
    }
    if (v !== passwordEl.value) {
        showHelper(passwordConfirmHelper, MSG.passwordConfirmMismatch);
        state.passwordConfirmOk = false;
        updateSubmitState();
        return false;
    }
    hideHelper(passwordConfirmHelper);
    state.passwordConfirmOk = true;
    updateSubmitState();
    return true;
}

async function validateNickname() {
    const raw = nicknameEl.value;
    // 빈값 처리 (터치된 경우에만 보여줌)
    if (!raw || raw.length === 0) {
        if (nicknameTouched) {
            showHelper(nicknameHelper, MSG.nicknameEmpty);
            state.nicknameOk = false;
            updateSubmitState();
        }
        return false;
    }
    // 공백 포함 검사
    if (/\s/.test(raw)) {
        showHelper(nicknameHelper, MSG.nicknameHasBlank);
        state.nicknameOk = false;
        updateSubmitState();
        return false;
    }
    const v = raw.trim();
    if (v.length > 10) {
        showHelper(nicknameHelper, MSG.nicknameTooLong);
        state.nicknameOk = false;
        updateSubmitState();
        return false;
    }
    try {
        const ok = await isNicknameAvailable(v);
        if (!ok) {
            showHelper(nicknameHelper, MSG.nicknameExisting);
            state.nicknameOk = false;
            updateSubmitState();
            return false;
        }
        hideHelper(nicknameHelper);
        state.nicknameOk = true;
        updateSubmitState();
        return true;
    } catch {
        showHelper(nicknameHelper, MSG.nicknameExisting);
        state.nicknameOk = false;
        updateSubmitState();
        return false;
    }
}

function validateAvatar() {
    const file = avatarInput.files?.[0];
    if (!file) {
        showHelper(avatarLabelHelper, MSG.avatarRequired);
        state.avatarOk = false;
        updateSubmitState();
        return false;
    }
    hideHelper(avatarLabelHelper);
    state.avatarOk = true;
    updateSubmitState();
    return true;
}

// 아바타: 미리보기 표시/제거
function setAvatarPreview(file) {
    const label = avatarTrigger;
    if (file) {
        const url = URL.createObjectURL(file);
        label.style.backgroundImage = `url('${url}')`;
        label.style.backgroundSize = "cover";
        label.style.backgroundPosition = "center";
        label.classList.add("has-image");
        // 내부 텍스트 제거
        const span = label.querySelector("span");
        if (span) span.textContent = "";
    } else {
        label.style.backgroundImage = "";
        label.classList.remove("has-image");
        const span = label.querySelector("span");
        if (span) span.textContent = "＋";
    }
}

const debouncedEmailCheck = debounce(() => {
    // 입력 중에는 먼저 로컬 유효성 메시지(형식 오류)를 보여주고,
    // 형식이 통과되면 서버 중복 검사 호출
    const raw = emailEl.value;
    const v = raw.trim();
    if (raw.length === 0) {
        // 이미 터치된 상태에서 빈 값이면 빈값 메시지
        if (emailTouched) {
            showHelper(emailHelper, MSG.emailEmpty);
            state.emailOk = false;
            updateSubmitState();
        }
        return;
    }
    // 로컬 형식 검사 시점: API 호출 전까지는 invalid 메시지 노출

    const emailCheck = isValidEmail(v);
    if (!emailCheck.ok) {
        if(emailCheck.reason === "empty") {
            showHelper(emailHelper, MSG.emailEmpty);
        } else {
            showHelper(emailHelper, MSG.emailInvalid);
        }
        state.emailOk = false;
        updateSubmitState();
        return;
    }
    // 형식 통과하면 API 호출
    validateEmail();
}, 500);

const debouncedNicknameCheck = debounce(() => {
    // 빈값/공백 포함/길이 검사 후, 모두 통과하면 API 호출
    const raw = nicknameEl.value;
    if (raw.length === 0) {
        if (nicknameTouched) {
            showHelper(nicknameHelper, MSG.nicknameEmpty);
            state.nicknameOk = false;
            updateSubmitState();
        }
        return;
    }
    if (/\s/.test(raw)) {
        showHelper(nicknameHelper, MSG.nicknameHasBlank);
        state.nicknameOk = false;
        updateSubmitState();
        return;
    }
    const v = raw.trim();
    if (v.length > 10) {
        showHelper(nicknameHelper, MSG.nicknameTooLong);
        state.nicknameOk = false;
        updateSubmitState();
        return;
    }
    // 모두 통과하면 서버 중복 체크
    validateNickname();
}, 500);

// 이벤트
emailEl.addEventListener("input", () => {
    // 사용자가 처음으로 입력을 시작하면 touch 상태로 변경
    if (!emailTouched) emailTouched = true;
    // 입력 중에는 API에 의존하지 않는 로컬 메시지/상태를 바로 반영
    state.emailOk = false;
    updateSubmitState();
    debouncedEmailCheck();
});

passwordEl.addEventListener("input", () => {
    validatePasswordSync();
    // 비밀번호 변경 시 확인도 다시 검증
    if (passwordConfirmEl.value) validatePasswordConfirmSync();
});
passwordConfirmEl.addEventListener("input", () => {
    validatePasswordConfirmSync();
});
nicknameEl.addEventListener("input", () => {
    // 터치 플래그
    if (!nicknameTouched) nicknameTouched = true;
    state.nicknameOk = false;
    updateSubmitState();
    debouncedNicknameCheck();
});
avatarInput.addEventListener("change", () => {
    const file = avatarInput.files?.[0] || null;
    setAvatarPreview(file);
    validateAvatar();
});

// 아바타 트리거를 클릭 시, 이미지가 이미 있으면 토글로 제거
avatarTrigger.addEventListener("click", (e) => {
    if (avatarTrigger.classList.contains("has-image")) {
        // 파일 선택 열지 않고 제거
        e.preventDefault();
        avatarInput.value = "";
        setAvatarPreview(null);
        validateAvatar(); // 제거 후 검증(필수)
    }
});

// 제출
formEl.addEventListener("submit", async (e) => {
    e.preventDefault();

    const [okPwd, okPwdCfm] = [validatePasswordSync(), validatePasswordConfirmSync()];
    const [okAvatar] = [validateAvatar()];
    const [okEmail, okNickname] = await Promise.all([validateEmail(), validateNickname()]);
    if (!(okEmail && okPwd && okPwdCfm && okNickname && okAvatar)) {
        return;
    }

    const email = emailEl.value.trim();
    const password = passwordEl.value;
    const nickname = nicknameEl.value.trim();
    const image = avatarInput.files?.[0];

    try {
        const fd = formWithJson("dto", {email, password, nickname});
        appendFile(fd, "image", image);

        const res = await apiForm("/users", fd, {method: "POST"});
        if (res?.message !== "register_success") {
            throw new Error("register_protocol_error");
        }
        window.location.href = "login.html";
    } catch (err) {
        showHelper(nicknameHelper, MSG.signupFailed);
        console.error(err);
    }
});