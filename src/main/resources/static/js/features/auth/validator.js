export function isValidEmail(value) {
  if (!value || value.trim().length < 5) return false;
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(value.trim());
}

export function isValidPassword(value) {
  if (!value) return { ok: false, reason: "empty" };
  if (value.length < 8 || value.length > 20) return { ok: false, reason: "length" };
  const hasUpper = /[A-Z]/.test(value);
  const hasLower = /[a-z]/.test(value);
  const hasDigit = /[0-9]/.test(value);
  const hasSpecial = /[^A-Za-z0-9]/.test(value);
  const ok = hasUpper && hasLower && hasDigit && hasSpecial;
  return { ok, reason: ok ? null : "composition" };
}