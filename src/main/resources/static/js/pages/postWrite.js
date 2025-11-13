// 게시글 작성 페이지 스크립트
import { createPost } from "../features/posts/api.js";

// 요소 참조
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

// 유틸: 한글 등 결합 문자 고려 길이 계산
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

  if (title.length === 0) {
    titleErrors.push("제목을 비울 수 없어요.");
  }
  if (strLen(title) > TITLE_MAX) {
    titleErrors.push(`제목은 최대 ${TITLE_MAX}자까지 입력할 수 있어요.`);
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

function bindEvents() {
  if ($title) {
    $title.addEventListener("input", () => {
      enforceTitleLimit();
      validate();
    });
  }
  if ($content) $content.addEventListener("input", validate);

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
        await createPost(dto, imageFile);
        // 성공 시 목록으로 이동 (신규 글 ID를 반환하지 않으므로 리스트로 리다이렉트)
        location.href = "postList.html";
      } catch (err) {
        console.error(err);
        setSubmitEnabled(true);
        setBodyHelper("작성 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.");
      }
    });
  }
}

// 부트스트랩
(function boot() {
  setTitleHelper("");
  setBodyHelper(""); // 초기에는 헬퍼 숨김
  if ($fileMsg) $fileMsg.textContent = FILE_PLACEHOLDER; // 기본 파일 안내 문구
  // 입력 시작 전에 한번 제한 적용 (브라우저 자동완성 등 대비)
  enforceTitleLimit();
  bindEvents();
  // 시작 시에도 한 번 검증해서 버튼 상태 반영
  validate();
})();
