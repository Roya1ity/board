const ACCESS_TOKEN_KEY = "accessToken";
const USER_KEY = "user";

let accessToken = localStorage.getItem(ACCESS_TOKEN_KEY) || "";
let refreshPromise = null;

export const authStore = {
  token: () => accessToken,
  user: () => JSON.parse(localStorage.getItem(USER_KEY) || "null"),
  save(data) {
    accessToken = data.accessToken?.startsWith("Bearer ")
      ? data.accessToken
      : `Bearer ${data.accessToken}`;
    const user = { id: data.id, email: data.email, nick: data.nick, role: data.role };
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    return user;
  },
  clear() {
    accessToken = "";
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

async function parseResponse(response) {
  if (response.status === 204 || response.headers.get("content-length") === "0") return null;
  const contentType = response.headers.get("content-type") || "";
  return contentType.includes("application/json") ? response.json() : null;
}

async function refresh() {
  if (!refreshPromise) {
    refreshPromise = fetch("/api/auth/reissue", {
      method: "POST",
      credentials: "include",
    })
      .then(async (response) => {
        if (!response.ok) throw new Error("로그인이 필요합니다.");
        return authStore.save(await response.json());
      })
      .finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

export async function api(path, options = {}, retry = true) {
  const headers = new Headers(options.headers);
  if (accessToken) headers.set("Authorization", accessToken);
  const response = await fetch(path, { ...options, headers, credentials: "include" });

  if (response.status === 401 && retry && path !== "/api/auth/reissue") {
    try {
      await refresh();
      return api(path, options, false);
    } catch {
      authStore.clear();
      return api(path, options, false);
    }
  }
  if (!response.ok) {
    const error = await parseResponse(response).catch(() => null);
    throw new Error(error?.msg || error?.message || error?.code || "요청 처리에 실패했습니다.");
  }
  return parseResponse(response);
}

export function json(method, body) {
  return {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}

export function normalizePage(data) {
  return {
    items: data?.content || data?.items || [],
    page: data?.number ?? data?.page ?? 0,
    totalPages: data?.totalPages ?? 0,
  };
}

export async function restoreSession() {
  try { return await refresh(); } catch { authStore.clear(); return null; }
}
