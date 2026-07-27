import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

const safeStorage = createJSONStorage(() => {
  const browserStorage = typeof window !== "undefined" ? window.localStorage : null;
  if (
    browserStorage &&
    typeof browserStorage.getItem === "function" &&
    typeof browserStorage.setItem === "function"
  ) {
    return browserStorage;
  }
  return { getItem: () => null, setItem: () => undefined, removeItem: () => undefined };
});

interface AuthUser {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

interface AuthState {
  user: AuthUser | null;
  userType: "customer" | "staff" | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: AuthUser, userType?: "customer" | "staff") => void;
  setAccessToken: (token: string | null) => void;
  clearUser: () => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      userType: null,
      accessToken: null,
      isAuthenticated: false,
      isLoading: true,
      setUser: (user, userType = "customer") =>
        set({ user, userType, isAuthenticated: true, isLoading: false }),
      setAccessToken: (accessToken) => set({ accessToken }),
      clearUser: () =>
        set({
          user: null,
          userType: null,
          accessToken: null,
          isAuthenticated: false,
          isLoading: false,
        }),
      setLoading: (isLoading) => set({ isLoading }),
    }),
    {
      name: "prestamos-facil-auth",
      storage: safeStorage,
      partialize: (state) => ({
        user: state.user,
        userType: state.userType,
        accessToken: state.accessToken,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);

export function checkIsAdmin(
  user: { email?: string; roles?: string[]; role?: string } | null | undefined,
  userType: string | null | undefined,
): boolean {
  if (!user) return false;

  const isStaffOrAdminType =
    userType === "staff" || userType === "admin" || userType === "ADMIN" || userType === "STAFF";

  if (!isStaffOrAdminType) return false;

  if (user.email?.toLowerCase() === "admin@prestamosfacil.com") {
    return true;
  }

  const roleList = Array.isArray(user.roles) ? user.roles : user.role ? [user.role] : [];

  const adminRoles = ["ADMIN", "admin", "Admin", "ROLE_ADMIN", "role_admin"];
  return roleList.some((r) => adminRoles.includes(r));
}
