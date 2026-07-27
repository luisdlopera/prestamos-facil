"use client";

import { useState, useCallback } from "react";
import { errorService } from "./errorService";

interface UseAsyncActionOptions<T> {
  successMsg?: string;
  onSuccess?: (result: T) => void;
  onError?: (error: unknown) => void;
  showErrorToast?: boolean;
}

interface UseAsyncActionReturn<T> {
  run: (action: () => Promise<T>) => Promise<T | undefined>;
  isLoading: boolean;
  error: string | null;
  clearError: () => void;
}

export function useAsyncAction<T = void>(
  opts: UseAsyncActionOptions<T> = {},
): UseAsyncActionReturn<T> {
  const { successMsg, onSuccess, onError, showErrorToast = true } = opts;

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = useCallback(
    async (action: () => Promise<T>): Promise<T | undefined> => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await action();
        if (successMsg) errorService.toast.success(successMsg);
        onSuccess?.(result);
        return result;
      } catch (e) {
        onError?.(e);
        const normalized = errorService.normalize(e);
        setError(normalized.message);
        if (showErrorToast) errorService.toast.error(e);
        return undefined;
      } finally {
        setIsLoading(false);
      }
    },
    [successMsg, onSuccess, onError, showErrorToast],
  );

  const clearError = useCallback(() => setError(null), []);

  return { run, isLoading, error, clearError };
}
