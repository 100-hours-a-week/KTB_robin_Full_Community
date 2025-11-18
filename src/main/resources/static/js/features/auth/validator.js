export function isValidEmail(value) {
    if (!value) return {ok: false, reason: "empty"};
    if (value.trim().length < 5) return {ok: false, reason: "invalid"};
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const ok = re.test(value.trim());
    return {ok, reason: ok? null : "composition"};
}

export function isValidPassword(value) {
    if (!value) return {ok: false, reason: "empty"};
    if (value.length < 8 || value.length > 20) return {ok: false, reason: "invalid"};
    const hasUpper = /[A-Z]/.test(value);
    const hasLower = /[a-z]/.test(value);
    const hasDigit = /[0-9]/.test(value);
    const hasSpecial = /[^A-Za-z0-9]/.test(value);
    const ok = hasUpper && hasLower && hasDigit && hasSpecial;
    return {ok, reason: ok ? null : "composition"};
}