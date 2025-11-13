export function buildQuery(params = {}) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
        if (v === undefined || v === null) return;
        search.append(k, String(v));
    });
    const s = search.toString();
    return s ? `?${s}` : "";
}
