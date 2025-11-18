import {apiJson} from "../../core/apiClient.js";
import {tokenStorage} from "../../core/storage.js";

export async function login({email, password}) {
    const payload = await apiJson("/auth/login", {method: "POST", body: {email, password}});
    if (payload?.message !== "login_success" || !payload?.data?.access_token) throw new Error("login_protocol_error");
    const {token_type, access_token, profile_image_url} = payload.data;
    const bearer = `${token_type} ${access_token}`;
    tokenStorage.set(bearer);
    try {
        localStorage.removeItem("profile_image_url");
        if (profile_image_url && typeof profile_image_url === "string" && profile_image_url.trim().length > 0) {
            localStorage.setItem("profile_image_url", profile_image_url.trim());
        }
    } catch (_) {
    }
    return bearer;
}

export async function refresh() {
    const payload = await apiJson("/auth/refresh", {method: "POST", retryOn401: false});
    if (payload?.message !== "access_token_refreshed" || !payload?.data?.access_token) throw new Error("refresh_protocol_error");
    const {token_type, access_token} = payload.data;
    const bearer = `${token_type} ${access_token}`;
    tokenStorage.set(bearer);
    return bearer;
}

export async function logout() {
    try {
        await apiJson("/auth/logout", {method: "POST", retryOn401: false});
    } finally {
        tokenStorage.clear();
        try {
            localStorage.removeItem("profile_image_url");
        } catch (_) {
        }
    }
}