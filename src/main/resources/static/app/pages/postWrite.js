// 게시글 작성 페이지 스크립트
import { createPost } from "../features/posts/api.js";
import { createPostFormContext, FILE_PLACEHOLDER } from "./postFormCommon.js";

// 요소 참조
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
