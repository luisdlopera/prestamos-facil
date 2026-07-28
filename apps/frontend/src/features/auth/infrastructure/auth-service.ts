import { COLD_START_TIMEOUT, get, patch, post } from "@/lib/api/client";
import { useAuthStore } from "@/lib/stores/auth.store";

interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponsePayload {
  id?: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  role?: string;
  roles?: string[];
  accessToken?: string;
  token?: string;
  user?: LoginResponsePayload;
}

export interface AuthUserProfile {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  documentType: string;
  documentNumber: string;
  baseSalary: number;
  userType: string;
  role?: string;
}

interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  baseSalary: number;
  email: string;
}

interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UnifiedLoginResult {
  user: { id: string; name: string; email: string; roles: string[] };
  userType: "customer" | "staff";
  redirectUrl: string;
}

export async function loginUnified(data: LoginRequest): Promise<UnifiedLoginResult> {
  const response = await post<LoginResponsePayload>("/auth/login", data, {
    timeout: COLD_START_TIMEOUT,
  });
  const res = response.data;
  if (!res) throw new Error("Credenciales inválidas. Revisa tu correo y contraseña.");

  const token = res.accessToken || res.token;
  if (token) useAuthStore.getState().setAccessToken(token);

  const u = res.user || res;
  const roleStr = String(u.role || (u.roles && u.roles[0]) || "CUSTOMER").toUpperCase();
  const isStaff = ["ADMIN", "ANALYST", "ASESOR", "COLLECTOR", "STAFF"].includes(roleStr);
  return {
    user: {
      id: u.id || "",
      name: u.name || `${u.firstName || ""} ${u.lastName || ""}`.trim() || u.email || "",
      email: u.email || "",
      roles: u.roles?.length ? u.roles : [roleStr],
    },
    userType: isStaff ? "staff" : "customer",
    redirectUrl: isStaff ? "/dashboard" : "/my-loans",
  };
}

// Backwards-compatible alias; all callers use the same unified endpoint.
export const login = loginUnified;

export async function logout(): Promise<void> {
  await post("/auth/logout");
  useAuthStore.getState().clearUser();
}

export async function fetchProfile(): Promise<AuthUserProfile> {
  const response = await get<AuthUserProfile>("/auth/me");
  return response.data!;
}

export async function updateProfile(data: UpdateProfileRequest): Promise<AuthUserProfile> {
  const response = await patch<AuthUserProfile>("/auth/profile", data);
  return response.data!;
}

export async function changePassword(data: ChangePasswordRequest): Promise<void> {
  await post("/auth/change-password", data);
}
