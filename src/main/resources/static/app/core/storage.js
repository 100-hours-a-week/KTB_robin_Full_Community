// 액세스 토큰을 저장
const KEY = "access_token";

export const tokenStorage = {
    set(bearer) {
        localStorage.setItem(KEY, bearer);
    },
    get() {
        return localStorage.getItem(KEY);
    },
    clear() {
        localStorage.removeItem(KEY);
    }
}