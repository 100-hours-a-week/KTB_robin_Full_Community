import {API_BASE} from "./config.js";
import {tokenStorage} from "./storage.js";

async function doFetch(url, options = {}) {
    const init = {credentials: "include", ...options};
    init.headers = {Accept: "application/json", ...(init.headers || {})};

    const isForm = init.body && typeof FormData !== "undefined" && init.body instanceof FormData;
    if (!isForm) {
        init.headers["Content-Type"] = init.headers["Content-Type"] || "application/json";
    }

    const res = await fetch(url, init);
    const isJson = (res.headers.get("content-type") || "").includes("application/json");
    const payload = isJson ? await res.json() : null;
    return {res, payload};
}

function withAuthHeaders(headers = {}) {
    const bearer = tokenStorage.get();
    return bearer ? {...headers, Authorization: bearer} : headers;
}

async function refreshAccessToken() {
    const {res, payload} = await doFetch(`${API_BASE}/auth/refresh`, {method: "POST"});
    if (!res.ok) {
        const err = new Error(payload?.message || "refresh_failed");
        err.status = res.status;
        throw err;
    }
    const data = payload?.data;
    if (!data?.access_token || !data?.token_type) throw new Error("refresh_protocol_error");
    const bearer = `${data.token_type} ${data.access_token}`;
    tokenStorage.set(bearer);
    return bearer;
}

async function requestWithAuthRetry(url, init, headers, retryOn401 = true) {
    let {res, payload} = await doFetch(url, init);

    if (res.status === 401 && retryOn401 && payload?.message === "access_token_expired") {
        try {
            await refreshAccessToken();
            const retry = await doFetch(url, {...init, headers: withAuthHeaders(headers)});
            res = retry.res;
            payload = retry.payload;
        } catch (e) {
            const err = new Error(payload?.message || e?.message || "unauthorized");
            err.status = 401;
            throw err;
        }
    }

    if (!res.ok) {
        const err = new Error(payload?.message || "request_failed");
        err.status = res.status;
        err.payload = payload;
        throw err;
    }
    return payload;
}

export async function apiJson(path, {method = "GET", body, headers, retryOn401 = true} = {}) {
    const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
    const init = {
        method,
        headers: withAuthHeaders(headers),
        body: body == null ? undefined : typeof body === "string" ? body : JSON.stringify(body),
    };

    const payload = await requestWithAuthRetry(url, init, headers, retryOn401);
    return payload; // { message, data }
}

export async function apiForm(path, formData, {method = "POST", headers, retryOn401 = true} = {}) {
    const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
    const init = {method, headers: withAuthHeaders(headers), body: formData};

    return await requestWithAuthRetry(url, init, headers, retryOn401);
}