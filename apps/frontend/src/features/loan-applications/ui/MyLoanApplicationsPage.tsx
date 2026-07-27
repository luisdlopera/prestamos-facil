"use client";

import { useMemo, useState } from "react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn, StatusOption } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { fetchLoanApplications } from "../infrastructure/loan-applications-api";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";
import { useAuthStore } from "@/lib/stores/auth.store";

import { Eye } from "lucide-react";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";

const columns: ServerTableColumn<LoanApplicationDto>[] = [
  {
    key: "customerName",
    header: "Cliente",
    type: "text",
    sortable: true,
    searchable: true,
  },
  {
    key: "requestedAmount",
    header: "Monto Solicitado",
    type: "currency",
    sortable: true,
    searchable: false,
  },
  {
    key: "loanTypeName",
    header: "Tipo Préstamo",
    type: "text",
    searchable: true,
  },
  {
    key: "termInMonths",
    header: "Plazo (meses)",
    type: "number",
    sortable: true,
    searchable: false,
    align: "center",
  },
  {
    key: "status",
    header: "Estado",
    type: "status",
    sortable: true,
    searchable: false,
  },
  {
    key: "createdAt",
    header: "Fecha Solicitud",
    type: "datetime",
    sortable: true,
    searchable: false,
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
            label: "Ver Solicitud",
            icon: <Eye className="size-4" />,
            href: `/loan-details?id=${row.id}`,
          },
        ]}
        row={row}
      />
    ),
  },
];

const STATUS_OPTIONS: StatusOption[] = [
  { label: "Pendiente de Revisión", value: "PENDING_REVIEW" },
  { label: "Revisión Manual", value: "MANUAL_REVIEW" },
  { label: "Aprobado", value: "APPROVED" },
  { label: "Rechazado", value: "REJECTED" },
];

export function MyLoanApplicationsPage() {
  const user = useAuthStore((s) => s.user);
  const [selectedStatus, setSelectedStatus] = useState<string>("");

  const fetchMyData = useMemo(
    () =>
      async (params: ServerTableParams): Promise<ServerTableResult<LoanApplicationDto>> => {
        if (!user?.id) {
          return {
            data: [],
            pagination: {
              page: 0,
              perPage: 10,
              total: 0,
              totalPages: 0,
            },
          };
        }

        const response = await fetchLoanApplications({
          page: params.page,
          size: params.size,
          search: params.search,
          status: params.status ?? (selectedStatus || undefined),
          customerId: user.id,
        });

        return response;
      },
    [user?.id, selectedStatus],
  );

  return (
    <ServerTable<LoanApplicationDto>
      columns={columns}
      fetchFn={fetchMyData}
      deps={[user?.id]}
      searchPlaceholder="Buscar por cliente o tipo de préstamo..."
      emptyMessage="No tienes solicitudes registradas"
      statusOptions={STATUS_OPTIONS}
      selectedStatus={selectedStatus}
      onStatusChange={setSelectedStatus}
    />
  );
}
