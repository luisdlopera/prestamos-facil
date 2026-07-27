"use client";

import { Skeleton } from "@heroui/react";

interface PageSkeletonProps {
  variant?: "table" | "form" | "card" | "detail";
}

export function PageSkeleton({ variant = "table" }: PageSkeletonProps) {
  if (variant === "form") {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48 rounded-xl" />
        <div className="space-y-4 rounded-2xl border border-border bg-surface p-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="space-y-2">
              <Skeleton className="h-4 w-24 rounded-lg" />
              <Skeleton className="h-10 w-full rounded-xl" />
            </div>
          ))}
        </div>
        <div className="flex gap-3">
          <Skeleton className="h-10 w-28 rounded-xl" />
          <Skeleton className="h-10 w-28 rounded-xl" />
        </div>
      </div>
    );
  }

  if (variant === "card") {
    return (
      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="space-y-3 rounded-2xl border border-border bg-surface p-6">
            <Skeleton className="h-4 w-24 rounded-lg" />
            <Skeleton className="h-8 w-32 rounded-lg" />
          </div>
        ))}
      </div>
    );
  }

  if (variant === "detail") {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-64 rounded-xl" />
        <div className="space-y-4 rounded-2xl border border-border bg-surface p-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="flex justify-between">
              <Skeleton className="h-5 w-32 rounded-lg" />
              <Skeleton className="h-5 w-48 rounded-lg" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Skeleton className="h-8 w-48 rounded-xl" />
        <Skeleton className="h-10 w-32 rounded-xl" />
      </div>
      <Skeleton className="h-10 w-72 rounded-xl" />
      <div className="rounded-2xl border border-border bg-surface">
        {Array.from({ length: 5 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-4 border-b border-border px-6 py-4 last:border-b-0"
          >
            <Skeleton className="h-5 flex-1 rounded-lg" />
            <Skeleton className="h-5 w-24 rounded-lg" />
            <Skeleton className="h-5 w-20 rounded-lg" />
            <Skeleton className="h-6 w-20 rounded-full" />
          </div>
        ))}
      </div>
      <div className="flex justify-center">
        <Skeleton className="h-8 w-64 rounded-lg" />
      </div>
    </div>
  );
}
