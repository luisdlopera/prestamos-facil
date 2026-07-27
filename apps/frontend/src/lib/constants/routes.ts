export const PUBLIC_ROUTES = [
  "/login",
  "/auth/register",
  "/auth/forgot-password",
  "/diagrama-relacional",
] as const;

export function isPublicPath(path: string | undefined | null): boolean {
  if (!path) return false;
  const cleanPath = path.split("?")[0].split("#")[0];
  return PUBLIC_ROUTES.some((route) => cleanPath === route || cleanPath.startsWith(`${route}/`));
}
