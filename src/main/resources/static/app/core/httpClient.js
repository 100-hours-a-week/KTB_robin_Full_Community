// export async function http(path, options = {}) {
//     const res = await fetch(path, {
//         credentials: "include",
//         headers: {
//             "Accept": "application/json",
//             "Content-Type": "application/json",
//             ...(options.headers || {}),
//         },
//         ...options,
//     });
//
//     const isJson = (res.headers.get("content-type") || "").includes("application/json");
//     const data = isJson ? await res.json() : null;
//
//     if (!res.ok) {
//         const err = new Error(data?.message || "request_failed");
//         err.status = res.status;
//         err.payload = data;
//         throw err;
//     }
//     return data;
// }