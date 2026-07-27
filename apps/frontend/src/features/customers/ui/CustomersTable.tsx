"use client";

import { useCallback } from "react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { fetchCustomers } from "../infrastructure/customers-api";
import type { CustomerDto } from "../infrastructure/customers-api";
import { FileText, FilePlus } from "lucide-react";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";

const columns: ServerTableColumn<CustomerDto>[] = [
  {
    key: "firstName",
    header: "Nombre",
    type: "custom",
    render: (row) => (
      <div className="font-semibold text-slate-900">
        {row.firstName} {row.lastName}
      </div>
    ),
  },
  { key: "email", header: "Correo Electrónico", type: "email" },
  {
    key: "documentType",
    header: "Identificación",
    type: "custom",
    render: (row) => (
      <div className="inline-flex items-center gap-1.5 font-medium text-xs">
        <span className="rounded bg-slate-100 px-1.5 py-0.5 font-bold uppercase text-slate-700">
          {row.documentType}
        </span>
        <span className="text-slate-600">{row.documentNumber}</span>
      </div>
    ),
  },
  {
    key: "baseSalary",
    header: "Salario Base",
    type: "currency",
    sortable: true,
  },
  {
    key: "actions" as const,
    header: "Acciones",
    type: "custom",
    render: (row) => (
      <TableActionsDropdown
        actions={[
          {
            key: "applications",
            label: "Ver Solicitudes",
            icon: <FileText className="size-4" />,
            href: `/loan-applications?search=${encodeURIComponent(row.email)}`,
          },
          {
            key: "new-application",
            label: "Nueva Solicitud",
            icon: <FilePlus className="size-4" />,
            href: `/new-loan-application?customerId=${row.id}`,
          },
        ]}
        row={row}
      />
    ),
  },
];

export function CustomersTable() {
  const fetchFn = useCallback(
    async (params: ServerTableParams): Promise<ServerTableResult<CustomerDto>> => {
      const response = await fetchCustomers({
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
    <ServerTable<CustomerDto>
      columns={columns}
      fetchFn={fetchFn}
      searchPlaceholder="Buscar por nombre, documento o correo..."
      emptyMessage="No hay clientes registrados en el sistema"
    />
  );
}
