"use client";

import type { ReactNode } from "react";
import { useEffect, useState } from "react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { fetchProfile } from "@/features/auth/infrastructure/auth-service";
import { Spinner } from "@heroui/react";

import { isPublicPath } from "@/lib/constants";

interface AuthProviderProps {
  children: ReactNode;
  publicRoutes?: string[];
  currentPath?: string;
}

export function AuthProvider({ children, publicRoutes = [], currentPath }: AuthProviderProps) {
  const [initialized, setInitialized] = useState(false);
  const setUser = useAuthStore((s) => s.setUser);
  const clearUser = useAuthStore((s) => s.clearUser);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  useEffect(() => {
    async function checkSession() {
      try {
        const profile = await fetchProfile();

        if (profile) {
          const roleUpper = String(profile.role || "").toUpperCase();
          const isStaffOrAdmin =
            ["ADMIN", "ANALYST", "ASESOR", "COLLECTOR", "STAFF"].includes(roleUpper) ||
            profile.email?.toLowerCase() === "admin@prestamosfacil.com" ||
            profile.userType === "staff";

          const userType: "customer" | "staff" = isStaffOrAdmin ? "staff" : "customer";
          const rolesList = isStaffOrAdmin
            ? roleUpper === "ADMIN" || profile.email?.toLowerCase() === "admin@prestamosfacil.com"
              ? ["ADMIN", "STAFF"]
              : [roleUpper || "STAFF", "STAFF"]
            : ["CUSTOMER"];

          setUser(
            {
              id: profile.id,
              name: (profile.firstName && profile.lastName
                ? `${profile.firstName} ${profile.lastName}`
                : profile.email
              ).trim(),
              email: profile.email,
              roles: rolesList,
            },
            userType,
          );
        } else {
          clearUser();
        }
      } catch {
        clearUser();
      } finally {
        setInitialized(true);
      }
    }
    checkSession();
  }, [setUser, clearUser]);

  const activePath = currentPath ?? (typeof window !== "undefined" ? window.location.pathname : "");
  const isPublicRoute =
    isPublicPath(activePath) || publicRoutes.some((route) => activePath.startsWith(route));

  if (!initialized && !isPublicRoute) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!isPublicRoute && initialized && !isAuthenticated) {
    if (typeof window !== "undefined") {
      const activeBrowserPath = window.location.pathname;
      if (!isPublicPath(activeBrowserPath)) {
        window.location.href = "/login";
      }
    }
    return null;
  }

  if (initialized && isAuthenticated && typeof window !== "undefined") {
    const activeUser = useAuthStore.getState().user;
    const activeUserType = useAuthStore.getState().userType;
    const isStaffOrAdmin =
      activeUserType === "staff" ||
      (activeUser?.roles &&
        activeUser.roles.some((r) =>
          ["STAFF", "ANALYST", "ASESOR", "COLLECTOR", "ADMIN"].includes(r.toUpperCase()),
        ));

    const staffOnlyRoutes = [
      "/dashboard",
      "/customers",
      "/register-customer",
      "/loan-applications",
      "/loans",
      "/reports",
      "/loan-types",
    ];
    const customerOnlyRoutes = ["/my-loans", "/my-loan-applications"];

    if (!isStaffOrAdmin && staffOnlyRoutes.some((r) => activePath.startsWith(r))) {
      window.location.href = "/my-loans";
      return null;
    }

    if (isStaffOrAdmin && customerOnlyRoutes.some((r) => activePath.startsWith(r))) {
      window.location.href = "/dashboard";
      return null;
    }
  }

  return <>{children}</>;
}
