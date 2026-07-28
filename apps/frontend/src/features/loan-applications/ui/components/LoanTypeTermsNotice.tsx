import { AlertCircle } from "lucide-react";
import type { LoanTypeDto } from "@/lib/api/loan-types-api";
import { formatCurrency } from "@/lib/formatters/currency";

interface LoanTypeTermsNoticeProps {
  loanType: LoanTypeDto;
}

function formatTerm(months: number): string {
  return `${months} ${months === 1 ? "mes" : "meses"}`;
}

/** Presents the configurable conditions of the currently selected loan type. */
export function LoanTypeTermsNotice({ loanType }: LoanTypeTermsNoticeProps) {
  return (
    <div className="flex items-start gap-2 rounded-xl bg-blue-50/80 border border-blue-100 p-3 text-xs text-blue-900">
      <AlertCircle className="size-4 text-blue-600 shrink-0 mt-0.5" aria-hidden="true" />
      <div>
        <span className="font-semibold">{loanType.name}:</span> Monto permitido desde{" "}
        <span className="font-bold">{formatCurrency(loanType.minAmount)}</span> hasta{" "}
        <span className="font-bold">{formatCurrency(loanType.maxAmount)}</span> con tasa del{" "}
        <span className="font-bold">{loanType.interestRate}% anual</span>. Plazo permitido de{" "}
        <span className="font-bold">
          {formatTerm(loanType.minTermMonths)} a {formatTerm(loanType.maxTermMonths)}
        </span>
        .
        {loanType.automaticValidationEnabled && (
          <span className="ml-1 font-semibold text-emerald-700">
            (Aprobación automática disponible)
          </span>
        )}
      </div>
    </div>
  );
}
