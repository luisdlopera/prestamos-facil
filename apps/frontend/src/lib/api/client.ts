import { env } from "../config/env";
import { useAuthStore } from "../stores/auth.store";
import type { ApiResponse, PaginatedApiResponse } from "./types";
import { normalizeApiError, handleNetworkError, ApiError } from "../errors";
import { isPublicPath } from "../constants";

const DEFAULT_TIMEOUT = 15_000;
const MAX_RETRIES = 1;
const RETRYABLE_STATUSES = [408, 429, 500, 502, 503, 504];
const RETRY_DELAY_MS = 500;

const REFRESH_PATHS = [
  "/auth/refresh",
  "/auth/login",
  "/auth/logout",
];

let refreshPromise: Promise<boolean> | null = null;

interface RequestOptions {
  timeout?: number;
  headers?: Record<string, string>;
}

async function request<T>(
  method: string,
  path: string,
  body?: unknown,
  options?: RequestOptions,
  retryCount = 0,
): Promise<ApiResponse<T>> {
  const url = `${env.apiBaseUrl}${path}`;
  const timeout = options?.timeout ?? DEFAULT_TIMEOUT;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  try {
    const token = useAuthStore.getState().accessToken;
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      ...options?.headers,
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const isRefreshPath = REFRESH_PATHS.some((p) => path.startsWith(p));
    const fetchOptions: RequestInit = {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    };

    // La autenticación usa cookies HttpOnly además del access token en memoria.
    // Las credenciales deben viajar también en /me, operaciones de negocio y logout.
    fetchOptions.credentials = "include";

    const response = await fetch(url, fetchOptions);

    clearTimeout(timeoutId);

    if (!response.ok) {
      const apiError = await normalizeApiError(response);

      if (
        (apiError.status === 401 || apiError.status === 403) &&
        !isRefreshPath &&
        retryCount < 1 &&
        typeof window !== "undefined"
      ) {
        const refreshed = await attemptTokenRefresh();
        if (refreshed) {
          return request<T>(method, path, body, options, retryCount + 1);
        }
        const currentPath = window.location.pathname;
        if (!isPublicPath(currentPath)) {
          window.location.href = "/login";
        }
      }

      if (retryCount < MAX_RETRIES && RETRYABLE_STATUSES.includes(apiError.status)) {
        await new Promise((resolve) => setTimeout(resolve, RETRY_DELAY_MS * (retryCount + 1)));
        return request<T>(method, path, body, options, retryCount + 1);
      }

      throw apiError;
    }

    if (response.status === 204 || response.headers.get("content-length") === "0") {
      return { data: null, message: "", timestamp: new Date().toISOString() } as ApiResponse<T>;
    }

    const text = await response.text();
    if (!text) {
      return { data: null, message: "", timestamp: new Date().toISOString() } as ApiResponse<T>;
    }

    return JSON.parse(text);
  } catch (error) {
    clearTimeout(timeoutId);
    if (error instanceof DOMException && error.name === "AbortError") {
      throw new ApiError({
        status: 0,
        code: "NETWORK_TIMEOUT",
        message: "La solicitud excedió el tiempo de espera.",
        retryable: true,
      });
    }
    if (error instanceof ApiError) throw error;
    throw handleNetworkError(error);
  }
}

async function attemptTokenRefresh(): Promise<boolean> {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    try {
      const response = await fetch(`${env.apiBaseUrl}/auth/refresh`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (response.ok) {
        const data = await response.json();
        const newToken = data.data?.accessToken ?? data.accessToken;
        if (newToken) {
          useAuthStore.getState().setAccessToken(newToken);
          return true;
        }
      }
      return false;
    } catch {
      return false;
    }
    finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

export async function get<T>(path: string, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<T>("GET", path, undefined, options);
}

export async function getPaginated<T>(
  path: string,
  options?: RequestOptions,
): Promise<PaginatedApiResponse<T>> {
  const response = await request<T[]>("GET", path, undefined, options);
  return response as unknown as PaginatedApiResponse<T>;
}

export async function post<T>(
  path: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<ApiResponse<T>> {
  return request<T>("POST", path, body, options);
}

export async function put<T>(
  path: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<ApiResponse<T>> {
  return request<T>("PUT", path, body, options);
}

export async function del<T>(path: string, options?: RequestOptions): Promise<ApiResponse<T>> {
  return request<T>("DELETE", path, undefined, options);
}

export async function patch<T>(
  path: string,
  body?: unknown,
  options?: RequestOptions,
): Promise<ApiResponse<T>> {
  return request<T>("PATCH", path, body, options);
}
