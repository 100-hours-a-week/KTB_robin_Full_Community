import { apiJson } from "../../core/apiClient.js";
import { buildQuery } from "../../core/url.js";

export async function isEmailAvailable(email) {
    try {
        const res = await apiJson(`/availability/email${buildQuery({ value: email })}`);
        return res?.message === "valid_email";
    } catch (e) {
        if (e.status === 409 && e.message === "existing_email") return false;
        throw e;
    }
}

export async function isNicknameAvailable(nickname) {
    try {
        const res = await apiJson(`/availability/nickname${buildQuery({ value: nickname })}`);
        return res?.message === "valid_nickname";
    } catch (e) {
        if (e.status === 409 && e.message === "existing_nickname") return false;
        throw e;
    }
}