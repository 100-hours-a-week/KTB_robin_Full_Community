import { apiJson, apiForm } from "../../core/apiClient.js";

// 나의 이메일/닉네임 조회
export async function fetchMyEditInfo() {
    const res = await apiJson("/users/me");
    if (!res?.data) throw new Error("userinfo_protocol_error");
    return res.data; // { email, nickname }
}

// 닉네임 수정
export async function updateNickname(newNickname) {
    const res = await apiJson("/users/me/nickname", {
        method: "PATCH",
        body: { newNickname },
    });
    return res?.data; // { newNickname }
}

// 비밀번호 수정
export async function updatePassword(newPassword) {
    await apiJson("/users/me/password", {
        method: "PATCH",
        body: { newPassword },
    });
    return true;
}

// 프로필 이미지 추가
export async function uploadProfileImage(file) {
    const fd = new FormData();
    fd.append("profile_image", file);
    const res = await apiForm("/users/me/profile-image", fd, { method: "PATCH" });
    return res?.data; // { image_url: ... } (컨트롤러에선 ProfileImageUrlResponse)
}

// 프로필 이미지 삭제
export async function deleteProfileImage() {
    const res = await apiJson("/users/me/profile-image", { method: "DELETE" });
    return res?.data; // { image_url: ... } (삭제된 URL)
}

// 탈퇴
export async function withdraw() {
    await apiJson("/users/me", { method: "DELETE" });
    return true;
}