import { get, getPaginated } from "@/lib/api/client";
import type { PaginatedApiResponse } from "@/lib/api/types";

export interface LoanDto {
  id: string;
  loanApplicationId: string;
  customerId: string;
  customerName: string;
  customerIdentification: string;
  principalAmount: number;
  annualInterestRate: number;
  termInMonths: number;
  monthlyPayment: number;
  status: "APPROVED" | "PAID" | "CANCELLED" | "DEFAULTED";
  approvedAt: string;
}

export interface PaymentInstallmentDto {
  id: string;
  installmentNumber: number;
  dueDate: string;
  openingBalance: number;
  paymentAmount: number;
  principalAmount: number;
  interestAmount: number;
  closingBalance: number;
}

export interface LoanFilters {
  page: number;
  size: number;
  search?: string;
  customerId?: string;
}

export async function fetchLoans(filters: LoanFilters): Promise<PaginatedApiResponse<LoanDto>> {
  const params = new URLSearchParams({ page: String(filters.page), size: String(filters.size) });
  if (filters.search) params.set("search", filters.search);
  if (filters.customerId) params.set("customerId", filters.customerId);
  return getPaginated<LoanDto>(`/loans?${params}`);
}

export async function fetchLoanById(id: string): Promise<LoanDto | null> {
  const response = await get<LoanDto>(`/loans/${id}`);
  return response.data;
}

export async function fetchPaymentPlan(loanId: string): Promise<PaymentInstallmentDto[]> {
  const response = await get<PaymentInstallmentDto[]>(`/loans/${loanId}/payment-plan`);
  return response.data ?? [];
}
