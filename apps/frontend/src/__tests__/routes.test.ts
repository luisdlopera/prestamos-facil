import { describe, it, expect } from "vitest";
import { isPublicPath, PUBLIC_ROUTES } from "../lib/constants/routes";

describe("isPublicPath helper", () => {
  it("should return true for exact public route matches", () => {
    expect(isPublicPath("/login")).toBe(true);
    expect(isPublicPath("/auth/register")).toBe(true);
    expect(isPublicPath("/auth/forgot-password")).toBe(true);
    expect(isPublicPath("/diagrama-relacional")).toBe(true);
  });

  it("should return true for public routes with query parameters or hash", () => {
    expect(isPublicPath("/login?redirect=/dashboard")).toBe(true);
    expect(isPublicPath("/auth/register?referral=123")).toBe(true);
  });

  it("should return false for protected routes", () => {
    expect(isPublicPath("/dashboard")).toBe(false);
    expect(isPublicPath("/customers")).toBe(false);
    expect(isPublicPath("/loans")).toBe(false);
    expect(isPublicPath("/loan-applications")).toBe(false);
    expect(isPublicPath("/profile")).toBe(false);
  });

  it("should return false for null, undefined, or empty path", () => {
    expect(isPublicPath("")).toBe(false);
    expect(isPublicPath(null)).toBe(false);
    expect(isPublicPath(undefined)).toBe(false);
  });

  it("should match all elements in PUBLIC_ROUTES constant", () => {
    PUBLIC_ROUTES.forEach((route) => {
      expect(isPublicPath(route)).toBe(true);
    });
  });
});
