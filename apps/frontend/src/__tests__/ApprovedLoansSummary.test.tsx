import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen, act } from "@testing-library/react";
import { ApprovedLoansSummary } from "../features/reports/ui/ApprovedLoansSummary";
import { useAuthStore } from "../lib/stores/auth.store";

vi.mock("../lib/stores/auth.store", async (importOriginal) => {
  const actual = await importOriginal<typeof import("../lib/stores/auth.store")>();
  return {
    ...actual,
    useAuthStore: vi.fn(),
  };
});

vi.mock("../features/reports/infrastructure/reports-api", () => ({
  fetchApprovedLoansTotal: vi.fn().mockResolvedValue({
    totalApprovedAmount: 50000000,
    approvedLoansCount: 10,
  }),
}));

vi.mock("../features/loan-applications/infrastructure/loan-applications-api", () => ({
  fetchLoanApplications: vi.fn().mockResolvedValue({
    data: [],
  }),
}));

vi.mock("../features/loans/infrastructure/loans-api", () => ({
  fetchLoans: vi.fn().mockResolvedValue({
    data: [],
  }),
}));

describe("ApprovedLoansSummary", () => {
  it("renders spinner while auth is loading", () => {
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({ user: null, userType: null, isLoading: true }),
    );

    render(<ApprovedLoansSummary />);
    // Spinner should render, no access restricted message yet
    expect(screen.queryByText(/Acceso Restringido/i)).not.toBeInTheDocument();
  });

  it("renders restricted access message for non-admin users", () => {
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({ user: { roles: ["CLIENT"] }, userType: "customer", isLoading: false }),
    );

    render(<ApprovedLoansSummary />);
    expect(screen.getByText(/Acceso Restringido/i)).toBeInTheDocument();
  });

  it("renders report dashboard with HeroUI DateRangePicker for admin users", async () => {
    (useAuthStore as unknown as ReturnType<typeof vi.fn>).mockImplementation(
      (selector: (state: Record<string, unknown>) => unknown) =>
        selector({
          user: { roles: ["ADMIN"], email: "admin@prestamosfacil.com" },
          userType: "staff",
          isLoading: false,
        }),
    );

    await act(async () => {
      render(<ApprovedLoansSummary />);
    });
    expect(screen.getByText(/Rango de Fechas:/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Aplicar Filtro/i })).toBeInTheDocument();
  });
});
