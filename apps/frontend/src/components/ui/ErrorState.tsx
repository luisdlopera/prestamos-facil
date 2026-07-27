"use client";

import { AlertTriangle, RefreshCw } from "lucide-react";
import { Button } from "@heroui/react";

interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="flex items-center gap-3 rounded-lg bg-red-50 dark:bg-red-950 p-4 text-sm text-red-600 dark:text-red-400">
      <AlertTriangle className="size-5 flex-shrink-0" />
      <span className="flex-1">{message}</span>
      {onRetry && (
        <Button size="sm" variant="ghost" onPress={onRetry} className="ml-auto">
          <RefreshCw className="size-4" />
          Reintentar
        </Button>
      )}
    </div>
  );
}
