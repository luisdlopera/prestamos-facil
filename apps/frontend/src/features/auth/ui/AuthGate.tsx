"use client";

import type { ReactNode } from "react";
import { useEffect } from "react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useNavigate } from "@/hooks/useNavigate";

interface AuthGateProps {
  children: ReactNode;
  fallback?: string;
}

export function AuthGate({ children, fallback = "/login" }: AuthGateProps) {
  const { isAuthenticated, isLoading } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      navigate(fallback);
    }
  }, [isAuthenticated, isLoading, fallback, navigate]);

  if (isLoading) return null;
  if (!isAuthenticated) return null;

  return <>{children}</>;
}
