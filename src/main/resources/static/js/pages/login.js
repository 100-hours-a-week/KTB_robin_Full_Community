const emailEl = document.getElementById("email");
const passwordEl = document.getElementById("password");
const helperEl = document.getElementById("helperText");
const loginBtn = document.getElementById("loginBtn");

const invalidEmailMessage = "*올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)";
const emptyPasswordMessage = "*비밀번호를 입력해주세요";
const invalidPasswordMessage = "*비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다.";
const invalidEmailOrPasswordMessage = "*올바른 이메일 혹은 비밀번호가 아닙니다.";
const loginFailedThenRetryMessage = "*로그인에 실패했습니다. 잠시 후 다시 시도해주세요.";



clearHelper();
setButtonBusy(true);

loginBtn.addEventListener("click", onSubmit);
emailEl.addEventListener("input", onTyping);
passwordEl.addEventListener("input", onTyping);

// 엔터키 입력도 제출 버튼으로 인식
[emailEl, passwordEl].forEach((el) =>
    el.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            loginBtn.click();
        }
    })
);


async function onSubmit() {

}

function onTyping() {

}

function showHelper(message) {
    helperEl.textContent = message;
    helperEl.classList.remove("is-hidden");
    helperEl.classList.add("is-visible");
}

function clearHelper() {
    helperEl.textContent = "";
    helperEl.classList.remove("is-visible");
    helperEl.classList.add("is-hidden");
}

function setButtonBusy(busy) {
    loginBtn.disabled = !!busy; // '!!' : 값을 명확하게 boolean 으로 변환하기
    loginBtn.classList.toggle("is-busy", !!busy);
}