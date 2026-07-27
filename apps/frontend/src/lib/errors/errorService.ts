import { toast } from "@heroui/react";
import { ApiError } from "./ApiError";
import { handleNetworkError } from "./normalizeApiError";

export const errorService = {
  normalize(error: unknown): ApiError {
    if (error instanceof ApiError) return error;
    if (error instanceof TypeError) return handleNetworkError(error);
    if (error instanceof Error) {
      return new ApiError({
        status: 0,
        code: "UNKNOWN_ERROR",
        message: error.message,
      });
    }
    if (typeof error === "string") {
      return new ApiError({
        status: 0,
        code: "UNKNOWN_ERROR",
        message: error,
      });
    }
    return new ApiError({
      status: 0,
      code: "UNKNOWN_ERROR",
      message: "Ocurrió un error inesperado.",
    });
  },

  toast: {
    error(errorOrMessage: unknown) {
      const apiError =
        typeof errorOrMessage === "string"
          ? new ApiError({ status: 0, code: "UNKNOWN_ERROR", message: errorOrMessage })
          : errorService.normalize(errorOrMessage);

      toast.danger(apiError.message);
      return apiError;
    },

    success(message: string) {
      toast.success(message);
    },

    warning(message: string) {
      toast.warning(message);
    },

    info(message: string) {
      toast.info(message);
    },
  },

  getFormErrors(error: unknown): Record<string, string> {
    const apiError = errorService.normalize(error);
    if (
      apiError.details &&
      typeof apiError.details === "object" &&
      !Array.isArray(apiError.details)
    ) {
      const details = apiError.details as Record<string, unknown>;
      if (details.errors && Array.isArray(details.errors)) {
        const result: Record<string, string> = {};
        for (const err of details.errors as Array<{ field?: string; message?: string }>) {
          if (err.field && err.message) {
            result[err.field] = err.message;
          }
        }
        return result;
      }
    }
    return {};
  },
};
