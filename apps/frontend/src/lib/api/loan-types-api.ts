import { get, post, put, patch, del } from "./client";
import type { Pagination } from "./types";

export interface LoanTypeDto {
  id: string;
  name: string;
  description: string;
  interestRate: number;
  rateType: "EA";
  minAmount: number;
  maxAmount: number;
  minTermMonths: number;
  maxTermMonths: number;
  displayOrder: number;
  active: boolean;
  automaticValidationEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateLoanTypePayload {
  name: string;
  description: string;
  interestRate: number;
  rateType: "EA";
  minAmount: number;
  maxAmount: number;
  minTermMonths: number;
  maxTermMonths: number;
  displayOrder: number;
  active: boolean;
  automaticValidationEnabled: boolean;
}

export type UpdateLoanTypePayload = CreateLoanTypePayload;

export interface FetchAdminLoanTypesParams {
  search?: string;
  active?: boolean;
  page?: number;
  size?: number;
}

export async function fetchLoanTypes(): Promise<LoanTypeDto[]> {
  try {
    const response = await get<LoanTypeDto[]>("/loan-types");
    return response.data ?? [];
  } catch (error) {
    console.error("Error fetching active loan types:", error);
    return [];
  }
}

export async function fetchAdminLoanTypes(
  params: FetchAdminLoanTypesParams = {},
): Promise<{ items: LoanTypeDto[]; pagination?: Pagination }> {
  const queryParams = new URLSearchParams();
  if (params.search) queryParams.set("search", params.search);
  if (params.active !== undefined) queryParams.set("active", String(params.active));
  if (params.page !== undefined) queryParams.set("page", String(params.page));
  if (params.size !== undefined) queryParams.set("size", String(params.size));

  const queryString = queryParams.toString();
  const path = `/loan-types/admin${queryString ? `?${queryString}` : ""}`;

  try {
    const response = await get<LoanTypeDto[]>(path);
    const items = Array.isArray(response.data) ? response.data : [];
    return {
      items,
      pagination: response.pagination,
    };
  } catch (err) {
    console.warn("fetchAdminLoanTypes failed, falling back to public /loan-types endpoint", err);
    const publicList = await fetchLoanTypes();
    return {
      items: publicList,
      pagination: {
        page: 0,
        perPage: publicList.length || 10,
        total: publicList.length,
        totalPages: 1,
        hasNext: false,
        hasPrevious: false,
      },
    };
  }
}

export async function createLoanType(payload: CreateLoanTypePayload): Promise<LoanTypeDto> {
  const response = await post<LoanTypeDto>("/loan-types", payload);
  if (!response.data) throw new Error("No data returned");
  return response.data;
}

export async function updateLoanType(
  id: string,
  payload: UpdateLoanTypePayload,
): Promise<LoanTypeDto> {
  const response = await put<LoanTypeDto>(`/loan-types/${id}`, payload);
  if (!response.data) throw new Error("No data returned");
  return response.data;
}

export async function toggleLoanTypeStatus(id: string, active: boolean): Promise<LoanTypeDto> {
  const response = await patch<LoanTypeDto>(`/loan-types/${id}/status`, { active });
  if (!response.data) throw new Error("No data returned");
  return response.data;
}

export async function reorderLoanTypes(orderedIds: string[]): Promise<void> {
  await post<void>("/loan-types/reorder", { orderedIds });
}

export async function deleteLoanType(id: string): Promise<void> {
  await del<void>(`/loan-types/${id}`);
}
