import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import "@testing-library/jest-dom";
import { LoanTypeTermsNotice } from "../features/loan-applications/ui/components/LoanTypeTermsNotice";
import type { LoanTypeDto } from "../lib/api/loan-types-api";

const loanType: LoanTypeDto = {
  id: "lt-1",
  name: "Préstamo Personal",
  description: "Uso libre",
  interestRate: 10,
  rateType: "EA",
  minAmount: 500_000,
  maxAmount: 30_000_000,
  minTermMonths: 6,
  maxTermMonths: 60,
  displayOrder: 1,
  active: true,
  automaticValidationEnabled: true,
};

describe("LoanTypeTermsNotice", () => {
  it("shows the configured term range dynamically", () => {
    render(<LoanTypeTermsNotice loanType={loanType} />);

    expect(screen.getByText(/Plazo permitido de/)).toHaveTextContent("6 meses a 60 meses");
    expect(screen.getByText(/Aprobación automática disponible/)).toBeInTheDocument();
  });

  it("uses singular text when a term is one month", () => {
    render(<LoanTypeTermsNotice loanType={{ ...loanType, minTermMonths: 1, maxTermMonths: 1 }} />);

    expect(screen.getByText(/Plazo permitido de/)).toHaveTextContent("1 mes a 1 mes");
  });
});

