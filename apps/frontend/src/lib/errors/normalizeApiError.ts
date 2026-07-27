import { ApiError } from "./ApiError";
import { ErrorCodes } from "./ErrorCodes";

const STATUS_MESSAGES: Record<number, string> = {
  400: "Datos inválidos. Revisa la información ingresada.",
  401: "Tu sesión ha expirado. Inicia sesión nuevamente.",
  403: "No tienes permiso para realizar esta acción.",
  404: "El recurso solicitado no existe o fue eliminado.",
  409: "Conflicto de datos. El registro ya existe o fue modificado.",
  422: "Datos inválidos. Revisa los campos marcados.",
  500: "Error interno del servidor. Intenta nuevamente.",
  503: "El servicio está temporalmente fuera de línea.",
};

function statusToCode(status: number, backendCode?: string): string {
  if (backendCode) return backendCode;
  switch (status) {
    case 400:
      return ErrorCodes.VALIDATION_ERROR;
    case 401:
      return ErrorCodes.UNAUTHORIZED;
    case 403:
      return ErrorCodes.FORBIDDEN;
    case 404:
      return ErrorCodes.RESOURCE_NOT_FOUND;
    case 409:
      return ErrorCodes.RESOURCE_CONFLICT;
    case 422:
      return ErrorCodes.VALIDATION_ERROR;
    case 500:
      return ErrorCodes.INTERNAL_ERROR;
    case 503:
      return ErrorCodes.SERVICE_UNAVAILABLE;
    default:
      return ErrorCodes.UNKNOWN_ERROR;
  }
}

function getDefaultMessage(status: number): string {
  return STATUS_MESSAGES[status] ?? `Error del servidor (${status}).`;
}

interface ParsedErrorBody {
  code?: string;
  message?: string;
  userMessage?: string;
  details?: unknown;
  error?: string | { code?: string; message?: string; userMessage?: string };
}

async function parseErrorBody(response: Response): Promise<ParsedErrorBody> {
  try {
    const text = await response.clone().text();
    if (!text.trim()) return {};
    const parsed = JSON.parse(text);
    if (parsed && typeof parsed === "object") return parsed as ParsedErrorBody;
    return {};
  } catch {
    return {};
  }
}

export async function normalizeApiError(response: Response): Promise<ApiError> {
  const body = await parseErrorBody(response);
  const status = response.status;

  const nested = body.error && typeof body.error === "object" ? body.error : undefined;

  const backendCode = nested?.code ?? body.code;
  const code = statusToCode(status, backendCode);

  const message =
    (nested as { userMessage?: string })?.userMessage ??
    nested?.message ??
    body.userMessage ??
    body.message ??
    (typeof body.error === "string" ? body.error : undefined) ??
    getDefaultMessage(status);

  const details = body.details ?? undefined;

  return new ApiError({ status, code, message, details });
}

export function handleNetworkError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;

  const message =
    error instanceof Error
      ? error.message.toLowerCase().includes("fetch") ||
        error.message.toLowerCase().includes("network")
        ? "Sin conexión al servidor. Verifica tu conexión."
        : error.message
      : "Error de conexión inesperado.";

  return new ApiError({
    status: 0,
    code: ErrorCodes.NETWORK_ERROR,
    message,
    retryable: true,
  });
}
