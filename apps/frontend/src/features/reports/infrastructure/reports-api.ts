import { get } from "@/lib/api/client";

export interface ApprovedLoansTotalDto {
  totalApprovedAmount: number;
  approvedLoansCount: number;
  generatedAt: string;
}

export async function fetchApprovedLoansTotal(): Promise<ApprovedLoansTotalDto | null> {
  const response = await get<ApprovedLoansTotalDto>("/reports/approved-loans/total");
  return response.data;
}
