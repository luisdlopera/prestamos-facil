"use client";

import { useCallback, useMemo } from "react";
import { LogOut, UserRound, Menu } from "lucide-react";
import { Button } from "@heroui/react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useSidebarStore } from "@/lib/stores/sidebar.store";
import { useAsyncAction } from "@/lib/errors";
import { useNavigate } from "@/hooks/useNavigate";
import { logout } from "@/features/auth";

export function HeaderActions() {
  const user = useAuthStore((s) => s.user);
  const clearUser = useAuthStore((s) => s.clearUser);
  const toggleMobileOpen = useSidebarStore((s) => s.toggleMobileOpen);
  const navigate = useNavigate();

  const handleLogoutSuccess = useCallback(() => {
    clearUser();
    navigate("/login");
  }, [clearUser, navigate]);

  const { run: runLogout, isLoading: isLoggingOut } = useAsyncAction({
    onSuccess: handleLogoutSuccess,
  });

  const handleProfileClick = useCallback(() => {
    navigate("/profile");
  }, [navigate]);

  const handleLogoutClick = useCallback(() => {
    runLogout(() => logout());
  }, [runLogout]);

  const userInitial = useMemo(() => {
    if (!user?.name) return "U";
    return user.name.charAt(0).toUpperCase();
  }, [user?.name]);

  if (!user) return null;

  return (
    <div className="flex items-center gap-2 sm:gap-3">
      <Button
        size="sm"
        variant="tertiary"
        aria-label="Abrir menú de navegación"
        className="h-9 px-2.5 md:hidden font-medium text-slate-700 hover:bg-slate-100"
        onPress={toggleMobileOpen}
      >
        <Menu className="size-5" />
      </Button>

      <div className="hidden items-center gap-2.5 rounded-full border border-slate-200 bg-slate-50 py-1 pl-1 pr-3 transition-colors hover:bg-slate-100 md:flex">
        <div className="flex size-7 items-center justify-center rounded-full bg-blue-600 font-semibold text-xs text-white shadow-xs">
          {userInitial}
        </div>
        <span className="max-w-[12rem] truncate text-xs font-semibold text-slate-800">
          {user.name}
        </span>
      </div>

      <Button
        size="sm"
        variant="tertiary"
        aria-label="Mi perfil"
        className="h-9 font-medium text-slate-700 hover:bg-slate-100 hover:text-slate-900"
        onPress={handleProfileClick}
      >
        <UserRound className="size-4 text-slate-500" />
        <span className="hidden sm:inline">Mi Perfil</span>
      </Button>

      <Button
        size="sm"
        variant="tertiary"
        aria-label="Cerrar sesión"
        className="h-9 font-medium text-rose-600 hover:bg-rose-50 hover:text-rose-700"
        isPending={isLoggingOut}
        onPress={handleLogoutClick}
      >
        <LogOut className="size-4" />
        <span className="hidden sm:inline">Cerrar sesión</span>
      </Button>
    </div>
  );
}
