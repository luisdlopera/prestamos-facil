import { getPaginated, post } from "@/lib/api/client";
import type { PaginatedApiResponse } from "@/lib/api/types";

export interface LoanApplicationDto {
  id: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  customerBaseSalary: number;
  loanTypeId: string;
  loanTypeName: string;
  annualInterestRate: number;
  requestedAmount: number;
  termInMonths: number;
  status: string;
  decisionReason: string | null;
  evaluatedAt: string | null;
  createdAt: string;
}

export interface LoanApplicationFilters {
  page: number;
  size: number;
  search?: string;
  status?: string;
  customerId?: string;
}

export async function fetchLoanApplications(
  filters: LoanApplicationFilters,
): Promise<PaginatedApiResponse<LoanApplicationDto>> {
  const params = new URLSearchParams({
    page: String(filters.page),
    size: String(filters.size),
  });
  if (filters.search) params.set("search", filters.search);
  if (filters.status) params.set("status", filters.status);
  if (filters.customerId) params.set("customerId", filters.customerId);

  return getPaginated<LoanApplicationDto>(`/loan-applications?${params}`);
}

export async function createLoanApplication(data: {
  customerId: string;
  loanTypeId: string;
  requestedAmount: number;
  termInMonths: number;
}): Promise<LoanApplicationDto> {
  const response = await post<LoanApplicationDto>("/loan-applications", data);
  return response.data!;
}

export async function approveLoanApplication(id: string): Promise<LoanApplicationDto> {
  const response = await post<LoanApplicationDto>(`/loan-applications/${id}/approve`);
  return response.data!;
}

export interface AutomaticEvaluationResponse {
  applicationId: string;
  decision: string;
  maxCapacity: number | null;
  currentDebt: number | null;
  newInstallment: number | null;
  debtRatio: number | null;
  reason: string | null;
}

export async function validateLoanApplicationAutomatically(
  id: string,
): Promise<AutomaticEvaluationResponse> {
  const response = await post<AutomaticEvaluationResponse>(
    `/loan-applications/${id}/automatic-evaluation`,
  );
  return response.data!;
}

export async function rejectLoanApplication(
  id: string,
  reason: string,
): Promise<LoanApplicationDto> {
  const response = await post<LoanApplicationDto>(`/loan-applications/${id}/reject`, { reason });
  return response.data!;
}
