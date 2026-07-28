import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { SidebarClient } from "../components/react/SidebarClient";
import { useAuthStore } from "../lib/stores/auth.store";
import { useSidebarStore } from "../lib/stores/sidebar.store";

vi.mock("../lib/stores/auth.store", async (importOriginal) => {
  const actual = await importOriginal<typeof import("../lib/stores/auth.store")>();
  return {
    ...actual,
    useAuthStore: vi.fn(),
  };
});

vi.mock("../lib/stores/sidebar.store", () => ({
  useSidebarStore: vi.fn(),
}));

describe("SidebarClient", () => {
  const setupSidebarMock = () => {
    (useSidebarStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({
          isCollapsed: false,
          toggleCollapsed: vi.fn(),
          isMobileOpen: false,
          setMobileOpen: vi.fn(),
        }),
    );
  };

  it("hides Tipos de Crédito and Administración section for credit advisors (non-admin staff)", () => {
    setupSidebarMock();
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({
          user: { roles: ["ASESOR"], email: "asesor@prestamosfacil.com" },
          userType: "staff",
        }),
    );

    render(<SidebarClient />);

    expect(screen.queryByText("Tipos de Crédito")).not.toBeInTheDocument();
    expect(screen.queryByText("Administración")).not.toBeInTheDocument();
    expect(screen.getByText("Clientes")).toBeInTheDocument();
    expect(screen.getByText("Solicitudes")).toBeInTheDocument();
  });

  it("shows Tipos de Crédito and Administración section for admin users", () => {
    setupSidebarMock();
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({
          user: { roles: ["ADMIN"], email: "admin@prestamosfacil.com" },
          userType: "staff",
        }),
    );

    render(<SidebarClient />);

    expect(screen.getByText("Tipos de Crédito")).toBeInTheDocument();
    expect(screen.getByText("Administración")).toBeInTheDocument();
  });

  it("shows Mis Préstamos, Mis Solicitudes, and Nueva Solicitud for customer users", () => {
    setupSidebarMock();
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({
          user: { roles: ["CUSTOMER"], email: "client@example.com" },
          userType: "customer",
        }),
    );

    render(<SidebarClient />);

    expect(screen.getByText("Mis Préstamos")).toBeInTheDocument();
    expect(screen.getByText("Mis Solicitudes")).toBeInTheDocument();
    expect(screen.getByText("Nueva Solicitud")).toBeInTheDocument();
    expect(screen.queryByText("Dashboard")).not.toBeInTheDocument();
  });
});
