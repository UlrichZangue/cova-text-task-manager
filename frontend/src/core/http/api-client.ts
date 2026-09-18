import { env } from "../../config/env";
import { tokenStorage } from "../auth/token-storage";
import { ApiError, type ApiErrorPayload } from "./api-error";

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  authenticated?: boolean;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (options.authenticated !== false) {
    const token = tokenStorage.get();
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${env.apiUrl}${path}`, {
      ...options,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new ApiError(0, {
      message: "API indisponible. Verifiez que le backend est demarre.",
    });
  }

  if (!response.ok) {
    const payload = (await response.json().catch(() => undefined)) as ApiErrorPayload | undefined;
    if (response.status === 401 && options.authenticated !== false) {
      tokenStorage.clear();
      window.dispatchEvent(new Event("auth:unauthorized"));
    }
    throw new ApiError(response.status, payload);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const apiClient = {
  get: <T>(path: string, authenticated = true) =>
    request<T>(path, { method: "GET", authenticated }),
  post: <T>(path: string, body: unknown, authenticated = true) =>
    request<T>(path, { method: "POST", body, authenticated }),
  put: <T>(path: string, body: unknown) => request<T>(path, { method: "PUT", body }),
  delete: (path: string) => request<void>(path, { method: "DELETE" }),
};
