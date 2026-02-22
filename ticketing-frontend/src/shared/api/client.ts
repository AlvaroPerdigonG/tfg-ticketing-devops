import { API_BASE_URL } from "./config";
import { toApiError } from "./errors";

type ApiClientOptions = {
  getToken?: () => string | null;
};

export function createApiClient(opts: ApiClientOptions) {
  async function request<T>(
    path: string,
    init?: RequestInit & { json?: unknown },
  ): Promise<T> {
    const url = path.startsWith("http") ? path : `${API_BASE_URL}${path}`;

    const headers = new Headers(init?.headers);

    const token = opts.getToken?.();
    if (token) headers.set("Authorization", `Bearer ${token}`);

    // JSON request
    if (init?.json !== undefined) {
      headers.set("Content-Type", "application/json");
    }

    const res = await fetch(url, {
      ...init,
      headers,
      body: init?.json !== undefined ? JSON.stringify(init.json) : init?.body,
    });

    if (!res.ok) throw await toApiError(res);

    // 204 No Content
    if (res.status === 204) return undefined as T;

    // Si no hay body, evitar error
    const text = await res.text();
    if (!text) return undefined as T;

    return JSON.parse(text) as T;
  }

  return {
    get: <T>(path: string) => request<T>(path, { method: "GET" }),
    post: <T>(path: string, json: unknown) => request<T>(path, { method: "POST", json }),
    patch: <T>(path: string, json: unknown) => request<T>(path, { method: "PATCH", json }),
  };
}