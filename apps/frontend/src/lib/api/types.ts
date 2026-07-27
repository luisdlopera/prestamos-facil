export interface ApiResponse<T> {
  data: T | null;
  message: string;
  timestamp: string;
  errors?: ErrorDetail[] | null;
  pagination?: Pagination;
}

export interface PaginatedApiResponse<T> extends Omit<ApiResponse<T[]>, "data"> {
  data: T[];
  pagination: Pagination;
}

export interface Pagination {
  page: number;
  perPage: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ErrorDetail {
  code: string;
  message: string;
  field?: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  errors?: ErrorDetail[];
}
