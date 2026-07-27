"use client";

import { useCallback } from "react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { fetchLoans } from "../infrastructure/loans-api";
import type { LoanDto } from "../infrastructure/loans-api";

import { Eye } from "lucide-react";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";

const columns: ServerTableColumn<LoanDto>[] = [
  {
    key: "customerName",
    header: "Nombre",
    type: "text",
  },
  {
    key: "customerIdentification",
    header: "Identificación",
    type: "text",
  },
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

export function LoansTable() {
  const fetchFn = useCallback(
    async (params: ServerTableParams): Promise<ServerTableResult<LoanDto>> => {
      const response = await fetchLoans({
        page: params.page,
        size: params.size,
        search: params.search,
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
    [],
  );

  return (
    <ServerTable<LoanDto>
      columns={columns}
      fetchFn={fetchFn}
      searchPlaceholder="Buscar por nombre, email o identificación..."
      emptyMessage="No hay préstamos registrados"
    />
  );
}
