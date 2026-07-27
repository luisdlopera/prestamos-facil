"use client";

import { useMemo, useCallback } from "react";
import { CheckCircle2, Eye, ShieldCheck, XCircle } from "lucide-react";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";
import type { TableAction } from "@/components/data-table/renderers/ActionsCell";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { fetchLoanApplications } from "../infrastructure/loan-applications-api";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";

const columns: ServerTableColumn<LoanApplicationDto>[] = [
  {
    key: "customerName",
    header: "Cliente",
    type: "text",
    sortable: true,
  },
  {
    key: "customerEmail",
    header: "Correo",
    type: "email",
  },
  {
    key: "customerBaseSalary",
    header: "Salario Base",
    type: "currency",
    sortable: true,
  },
  {
    key: "requestedAmount",
    header: "Monto Solicitado",
    type: "currency",
    sortable: true,
  },
  {
    key: "loanTypeName",
    header: "Tipo Préstamo",
    type: "text",
  },
  {
    key: "annualInterestRate",
    header: "Tasa",
    type: "custom",
    align: "center",
    render: (row) => {
      const rate = row.annualInterestRate;
      return (
        <span className="tabular-nums">
          {new Intl.NumberFormat("es-CO", {
            style: "percent",
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
          }).format(rate / 100)}
        </span>
      );
    },
  },
  {
    key: "termInMonths",
    header: "Plazo (meses)",
    type: "number",
    sortable: true,
    align: "center",
  },
  {
    key: "status",
    header: "Estado",
    type: "status",
    sortable: true,
  },
  {
    key: "createdAt",
    header: "Fecha Solicitud",
    type: "datetime",
    sortable: true,
  },
];

interface LoanApplicationsTableProps {
  defaultSearch?: string;
  onApprove?: (application: LoanApplicationDto) => void;
  onReject?: (application: LoanApplicationDto) => void;
  onAutoValidate?: (application: LoanApplicationDto) => void;
}

export function LoanApplicationsTable({
  defaultSearch,
  onApprove,
  onReject,
  onAutoValidate,
}: LoanApplicationsTableProps) {
  const getRowActions = useCallback(
    (row: LoanApplicationDto): TableAction<LoanApplicationDto>[] => {
      const list: TableAction<LoanApplicationDto>[] = [];

      if (row.status === "PENDING_REVIEW" || row.status === "MANUAL_REVIEW") {
        if (onAutoValidate) {
          list.push({
            key: "auto-validate",
            label: "Validar",
            icon: <ShieldCheck className="size-4" />,
            onAction: (r) => onAutoValidate(r),
          });
        }
        if (onApprove) {
          list.push({
            key: "approve",
            label: "Aprobar",
            icon: <CheckCircle2 className="size-4" />,
            onAction: (r) => onApprove(r),
          });
        }
        if (onReject) {
          list.push({
            key: "reject",
            label: "Rechazar",
            icon: <XCircle className="size-4" />,
            variant: "danger",
            onAction: (r) => onReject(r),
          });
        }
      }

      list.push({
        key: "view",
        label: "Ver detalles",
        icon: <Eye className="size-4" />,
        href: `/loan-details?id=${row.id}`,
      });

      return list;
    },
    [onApprove, onReject, onAutoValidate],
  );

  const allColumns: ServerTableColumn<LoanApplicationDto>[] = useMemo(
    () => [
      ...columns,
      {
        key: "actions" as const,
        header: "Acciones",
        type: "custom",
        render: (row: LoanApplicationDto) => (
          <TableActionsDropdown actions={getRowActions(row)} row={row} />
        ),
      },
    ],
    [getRowActions],
  );

  const statusOptions = useMemo(
    () => [
      { label: "Pendiente", value: "PENDING_REVIEW" },
      { label: "Revisión manual", value: "MANUAL_REVIEW" },
      { label: "Aprobada", value: "APPROVED" },
      { label: "Rechazada", value: "REJECTED" },
    ],
    [],
  );

  const fetchFn = useCallback(
    async (params: ServerTableParams): Promise<ServerTableResult<LoanApplicationDto>> => {
      const response = await fetchLoanApplications({
        page: params.page,
        size: params.size,
        search: params.search,
        status: params.status,
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
    <ServerTable<LoanApplicationDto>
      columns={allColumns}
      fetchFn={fetchFn}
      defaultSearch={defaultSearch}
      statusOptions={statusOptions}
      searchPlaceholder="Buscar por cliente, correo o monto..."
      emptyMessage="No hay solicitudes de préstamo registradas"
    />
  );
}
