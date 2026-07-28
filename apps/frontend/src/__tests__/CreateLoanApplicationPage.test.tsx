import { describe, it, expect, vi, beforeEach } from "vitest";
import "@testing-library/jest-dom";
import { render, screen, act } from "@testing-library/react";
import { CreateLoanApplicationPage } from "../features/loan-applications/ui/CreateLoanApplicationPage";
import { useAuthStore } from "../lib/stores/auth.store";

vi.mock("../features/customers/infrastructure/customers-api", () => ({
  fetchCustomers: vi.fn().mockResolvedValue({
    data: [
      { id: "cust-1", firstName: "Juan", lastName: "Pérez", email: "juan@example.com" },
      { id: "cust-2", firstName: "Maria", lastName: "Gomez", email: "maria@example.com" },
    ],
  }),
}));

vi.mock("../lib/api/loan-types-api", () => ({
  fetchLoanTypes: vi.fn().mockResolvedValue([
    {
      id: "lt-1",
      name: "Préstamo Personal",
      minimumAmount: { amount: 100000, currency: "COP" },
      maximumAmount: { amount: 5000000, currency: "COP" },
      annualInterestRate: 15,
      automaticValidationEnabled: true,
    },
  ]),
}));

describe("CreateLoanApplicationPage", () => {
  beforeEach(() => {
    useAuthStore.setState({
      userType: "staff",
      user: { id: "staff-1", name: "Staff User", roles: ["staff"], email: "staff@example.com" },
    });
  });

  it("renders applicant customer field with Autocomplete for staff users", async () => {
    await act(async () => {
      render(<CreateLoanApplicationPage />);
    });
    expect(screen.getByText("Nueva Solicitud de Préstamo")).toBeInTheDocument();
    expect(screen.getByText("Cliente Solicitante")).toBeInTheDocument();
    expect(screen.getByText("Buscar o seleccionar cliente...")).toBeInTheDocument();
  });
});
