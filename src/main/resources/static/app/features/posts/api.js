import { apiJson, apiForm } from "../../core/apiClient.js";
import { buildQuery } from "../../core/url.js";

// 목록
export async function fetchPosts({ after, limit }) {
    const res = await apiJson(`/posts${buildQuery({ after, limit })}`);
    return res?.data; // { posts, next_cursor, has_next }
}

// 상세
export async function fetchPostDetail(id) {
    const res = await apiJson(`/posts/${id}`);
    return res?.data; // { post, is_liked, isOwner, comments }
}

// 등록 (dto + image)
export async function createPost({ title, content }, imageFile) {
    const fd = new FormData();
    fd.append("dto", new Blob([JSON.stringify({ title, content })], { type: "application/json" }));
    if (imageFile) fd.append("image", imageFile);
    await apiForm("/posts", fd, { method: "POST" });
    return true;
}

// 수정 (dto + image, PATCH)
export async function editPost(id, { title, content }, imageFile) {
    const fd = new FormData();
    fd.append("dto", new Blob([JSON.stringify({ title, content })], { type: "application/json" }));
    if (imageFile) fd.append("image", imageFile);
    await apiForm(`/posts/${id}`, fd, { method: "PATCH" });
    return true;
}

// 삭제
export async function removePost(id) {
    await apiJson(`/posts/${id}`, { method: "DELETE" });
    return true;
}

// 좋아요/취소
export async function likePost(id) {
    await apiJson(`/posts/${id}/likes`, { method: "POST" });
    return true;
}
export async function unlikePost(id) {
    await apiJson(`/posts/${id}/likes`, { method: "DELETE" });
    return true;
}

// 댓글
export async function addComment(postId, content) {
    await apiJson(`/posts/${postId}/comments${buildQuery({ content })}`, { method: "POST" });
    return true;
}
export async function editComment(postId, commentId, content) {
    await apiJson(`/posts/${postId}/comments/${commentId}${buildQuery({ content })}`, { method: "PATCH" });
    return true;
}
export async function removeComment(postId, commentId) {
    await apiJson(`/posts/${postId}/comments/${commentId}`, { method: "DELETE" });
    return true;
}