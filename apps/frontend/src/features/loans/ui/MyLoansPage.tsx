"use client";

import { useMemo } from "react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { fetchLoans } from "../infrastructure/loans-api";
import type { LoanDto } from "../infrastructure/loans-api";
import { useAuthStore } from "@/lib/stores/auth.store";

import { Eye } from "lucide-react";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";

const columns: ServerTableColumn<LoanDto>[] = [
  {
    key: "principalAmount",
    header: "Monto",
    type: "currency",
    sortable: true,
  },
  {
    key: "annualInterestRate",
    header: "Tasa Anual",
    type: "custom",
    sortable: true,
    render: (row) => <span>{row.annualInterestRate}%</span>,
  },
  {
    key: "termInMonths",
    header: "Plazo (meses)",
    type: "text",
    sortable: true,
  },
  {
    key: "monthlyPayment",
    header: "Cuota Mensual",
    type: "currency",
  },
  {
    key: "approvedAt",
    header: "Fecha Aprobación",
    type: "datetime",
  },
  {
    key: "actions" as const,
    header: "Acciones",
    type: "custom",
    render: (row) => (
      <TableActionsDropdown
        actions={[
          {
            key: "details",
            label: "Ver Detalles",
            icon: <Eye className="size-4" />,
            href: `/loan-details?id=${row.id}`,
          },
        ]}
        row={row}
      />
    ),
  },
];

export function MyLoansPage() {
  const userId = useAuthStore((s) => s.user?.id);

  const fetchMyLoans = useMemo(
    () =>
      async (params: ServerTableParams): Promise<ServerTableResult<LoanDto>> => {
        if (!userId) {
          return { data: [], pagination: { page: 0, perPage: 10, total: 0, totalPages: 0 } };
        }

        const response = await fetchLoans({
          page: params.page,
          size: params.size,
          search: params.search,
          customerId: userId,
        });

        return {
          data: response.data ?? [],
          pagination: response.pagination ?? {
            page: 0,
            perPage: 10,
            total: 0,
            totalPages: 0,
          },
        };
      },
    [userId],
  );

  return (
    <ServerTable<LoanDto>
      columns={columns}
      fetchFn={fetchMyLoans}
      deps={[userId]}
      searchPlaceholder="Buscar préstamo..."
      emptyMessage="No hay préstamos registrados"
    />
  );
}
