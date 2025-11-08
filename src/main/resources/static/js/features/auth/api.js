import {http} from "../../core/httpClient.js";
import {tokenStorage} from "../../core/storage.js";
import {API_BASE} from "../../core/config.js";

export async function login({email, password}) {
    const payload = await http(`${API_BASE}/auth/login`, {
        method: "POST",
        body: JSON.stringify({email, password}),
    });

    if (payload?.message !== "login_success" || !payload?.data?.access_token) {
        throw new Error("login_protocol_error");
    }

    const bearer = `${payload.data.token_type} ${payload.data.access_token}`;
    tokenStorage.set(bearer);
    return bearer;
}
