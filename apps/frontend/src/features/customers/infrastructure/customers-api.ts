import { getPaginated, post } from "@/lib/api/client";
import type { PaginatedApiResponse } from "@/lib/api/types";

export interface CustomerDto {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  documentType: string;
  documentNumber: string;
  baseSalary: number;
  phoneCountryCode: string;
  phoneNumber: string;
}

export interface CreateCustomerData {
  firstName: string;
  lastName: string;
  email: string;
  documentType: string;
  documentNumber: string;
  baseSalary: number;
}

export interface CustomerFilters {
  page: number;
  size: number;
  search?: string;
}

export async function fetchCustomers(
  filters: CustomerFilters,
): Promise<PaginatedApiResponse<CustomerDto>> {
  const params = new URLSearchParams({ page: String(filters.page), size: String(filters.size) });
  if (filters.search) params.set("search", filters.search);
  return getPaginated<CustomerDto>(`/customers?${params}`);
}

export async function createCustomer(data: CreateCustomerData): Promise<CustomerDto> {
  const response = await post<CustomerDto>("/customers", data);
  return response.data!;
}
