"use client";

import { useState, useMemo, useCallback } from "react";
import { Button, Card, Chip, Spinner } from "@heroui/react";
import {
  Layers,
  Plus,
  Edit,
  Trash2,
  ArrowUp,
  ArrowDown,
  CheckCircle2,
  XCircle,
  ShieldAlert,
} from "lucide-react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import { TableActionsDropdown } from "@/components/data-table/renderers/ActionsCell";
import {
  fetchAdminLoanTypes,
  createLoanType,
  updateLoanType,
  toggleLoanTypeStatus,
  reorderLoanTypes,
  deleteLoanType,
  type LoanTypeDto,
  type CreateLoanTypePayload,
} from "@/lib/api/loan-types-api";
import { useAsyncAction } from "@/lib/errors";
import { formatCurrency } from "@/lib/formatters/currency";
import { useAuthStore, checkIsAdmin } from "@/lib/stores/auth.store";
import { LoanTypeFormModal } from "./LoanTypeFormModal";
import { DeleteLoanTypeModal } from "./DeleteLoanTypeModal";

const statusOptions = [
  { label: "Todos los estados", value: "" },
  { label: "Activos", value: "true" },
  { label: "Inactivos", value: "false" },
];

export function LoanTypesManagementPage() {
  const user = useAuthStore((s) => s.user);
  const userType = useAuthStore((s) => s.userType);
  const isAuthLoading = useAuthStore((s) => s.isLoading);

  const isAdmin = useMemo(() => checkIsAdmin(user, userType), [user, userType]);

  const [refreshKey, setRefreshKey] = useState(0);
  const [loanTypesList, setLoanTypesList] = useState<LoanTypeDto[]>([]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<LoanTypeDto | null>(null);
  const [deleteItem, setDeleteItem] = useState<LoanTypeDto | null>(null);

  const { run: runAction, isLoading } = useAsyncAction();

  const reloadTable = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  const fetchFn = useCallback(
    async (params: ServerTableParams): Promise<ServerTableResult<LoanTypeDto>> => {
      const activeParam =
        params.status === "true" ? true : params.status === "false" ? false : undefined;

      const response = await fetchAdminLoanTypes({
        page: params.page,
        size: params.size,
        search: params.search,
        active: activeParam,
      });

      const items = response.items ?? [];
      setLoanTypesList(items);

      return {
        data: items,
        pagination: response.pagination ?? {
          page: 0,
          perPage: 10,
          total: items.length,
          totalPages: 1,
          hasNext: false,
          hasPrevious: false,
        },
      };
    },
    [],
  );

  const handleFormSubmit = useCallback(
    (payload: CreateLoanTypePayload, id?: string) => {
      runAction(async () => {
        if (id) {
          await updateLoanType(id, payload);
        } else {
          await createLoanType(payload);
        }
        setIsModalOpen(false);
        reloadTable();
      });
    },
    [runAction, reloadTable],
  );

  const handleToggleStatus = useCallback(
    (item: LoanTypeDto) => {
      runAction(async () => {
        await toggleLoanTypeStatus(item.id, !item.active);
        reloadTable();
      });
    },
    [runAction, reloadTable],
  );

  const handleMoveOrder = useCallback(
    (item: LoanTypeDto, direction: "up" | "down") => {
      const index = loanTypesList.findIndex((t) => t.id === item.id);
      if (index === -1) return;
      if (
        (direction === "up" && index === 0) ||
        (direction === "down" && index === loanTypesList.length - 1)
      ) {
        return;
      }

      const targetIndex = direction === "up" ? index - 1 : index + 1;
      const newList = [...loanTypesList];
      newList[index] = newList[targetIndex];
      newList[targetIndex] = loanTypesList[index];

      const reorderedIds = newList.map((i) => i.id);
      setLoanTypesList(newList);

      runAction(async () => {
        await reorderLoanTypes(reorderedIds);
        reloadTable();
      });
    },
    [loanTypesList, runAction, reloadTable],
  );

  const handleDeleteConfirm = useCallback(() => {
    if (!deleteItem) return;
    runAction(async () => {
      await deleteLoanType(deleteItem.id);
      setDeleteItem(null);
      reloadTable();
    });
  }, [deleteItem, runAction, reloadTable]);

  const columns = useMemo(
    (): ServerTableColumn<LoanTypeDto>[] => [
      {
        key: "displayOrder",
        header: "Orden",
        type: "custom",
        render: (row) => {
          const index = loanTypesList.findIndex((item) => item.id === row.id);
          return (
            <div className="flex items-center justify-center gap-1.5">
              <span className="font-bold text-slate-700 w-4 text-center">{row.displayOrder}</span>
              <div className="flex flex-col gap-0.5">
                <button
                  onClick={() => handleMoveOrder(row, "up")}
                  disabled={index <= 0}
                  className="text-slate-400 hover:text-blue-600 disabled:opacity-30 cursor-pointer"
                  title="Mover arriba"
                >
                  <ArrowUp className="size-3.5" />
                </button>
                <button
                  onClick={() => handleMoveOrder(row, "down")}
                  disabled={index >= loanTypesList.length - 1}
                  className="text-slate-400 hover:text-blue-600 disabled:opacity-30 cursor-pointer"
                  title="Mover abajo"
                >
                  <ArrowDown className="size-3.5" />
                </button>
              </div>
            </div>
          );
        },
      },
      {
        key: "name",
        header: "Tipo de Crédito",
        searchable: true,
        type: "custom",
        render: (row) => (
          <div>
            <span className="font-semibold text-slate-900 block">{row.name}</span>
            {row.description && (
              <span className="text-xs text-slate-500 line-clamp-1">{row.description}</span>
            )}
          </div>
        ),
      },
      {
        key: "interestRate",
        header: "Tasa de Interés",
        type: "custom",
        render: (row) => (
          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-800">{row.interestRate}%</span>
            <Chip
              size="sm"
              variant="primary"
              className="font-bold text-xs"
            >
              {row.rateType || "EA"}
            </Chip>
          </div>
        ),
      },
      {
        key: "minAmount",
        header: "Rango de Monto",
        type: "custom",
        render: (row) => (
          <div className="text-xs">
            <span className="font-semibold text-slate-900">{formatCurrency(row.minAmount)}</span>
            <span className="text-slate-400 mx-1">a</span>
            <span className="font-semibold text-slate-900">{formatCurrency(row.maxAmount)}</span>
          </div>
        ),
      },
      {
        key: "minTermMonths",
        header: "Rango de Plazo",
        type: "custom",
        render: (row) => (
          <span className="text-xs font-medium text-slate-700">
            {row.minTermMonths} - {row.maxTermMonths} meses
          </span>
        ),
      },
      {
        key: "active",
        header: "Estado",
        type: "custom",
        render: (row) => (
          <button
            onClick={() => handleToggleStatus(row)}
            className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold transition-all cursor-pointer ${
              row.active
                ? "bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100"
                : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
            }`}
          >
            {row.active ? (
              <>
                <CheckCircle2 className="size-3.5 text-emerald-600" />
                Activo
              </>
            ) : (
              <>
                <XCircle className="size-3.5 text-slate-400" />
                Inactivo
              </>
            )}
          </button>
        ),
      },
      {
        key: "actions",
        header: "Acciones",
        type: "custom",
        render: (row) => (
          <TableActionsDropdown
            actions={[
              {
                key: "edit",
                label: "Editar Tipo de Crédito",
                icon: <Edit className="size-4" />,
                onAction: () => {
                  setEditingItem(row);
                  setIsModalOpen(true);
                },
              },
              {
                key: "toggle",
                label: row.active ? "Desactivar" : "Activar",
                icon: row.active ? (
                  <XCircle className="size-4" />
                ) : (
                  <CheckCircle2 className="size-4" />
                ),
                onAction: () => handleToggleStatus(row),
              },
              {
                key: "delete",
                label: "Eliminar",
                variant: "danger",
                icon: <Trash2 className="size-4 text-rose-500" />,
                onAction: () => setDeleteItem(row),
              },
            ]}
            row={row}
          />
        ),
      },
    ],
    [loanTypesList, handleMoveOrder, handleToggleStatus],
  );

  if (isAuthLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <Card className="w-full p-8 text-center border border-slate-200 bg-white rounded-2xl">
        <div className="flex flex-col items-center justify-center space-y-3">
          <div className="flex size-12 items-center justify-center rounded-2xl bg-rose-50 text-rose-600">
            <ShieldAlert className="size-6" />
          </div>
          <h2 className="text-lg font-bold text-slate-900">Acceso Restringido</h2>
          <p className="text-xs text-slate-500 max-w-md">
            La administración de tipos de crédito solo está disponible para usuarios con rol
            Administrador.
          </p>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between rounded-2xl bg-gradient-to-r from-slate-900 via-indigo-950 to-blue-900 p-6 text-white shadow-lg">
        <div className="flex items-center gap-4">
          <div className="flex size-14 items-center justify-center rounded-xl bg-white/10 backdrop-blur-md border border-white/20">
            <Layers className="size-7 text-blue-300" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">
              Administración de Tipos de Crédito
            </h1>
            <p className="text-sm text-slate-300">
              Gestiona las líneas de crédito, tasas de interés (EA/MV), montos y plazos disponibles.
            </p>
          </div>
        </div>
        <Button
          variant="primary"
          className="bg-blue-500 hover:bg-blue-600 font-semibold shadow-md gap-2"
          onPress={() => {
            setEditingItem(null);
            setIsModalOpen(true);
          }}
        >
          <Plus className="size-5" />
          Nuevo Tipo de Crédito
        </Button>
      </div>

      <Card className="p-4 border border-slate-200/80 bg-white rounded-xl shadow-xs">
        <ServerTable<LoanTypeDto>
          key={refreshKey}
          columns={columns}
          fetchFn={fetchFn}
          searchPlaceholder="Buscar por nombre o descripción..."
          emptyMessage="No hay tipos de crédito registrados"
          statusOptions={statusOptions}
        />
      </Card>

      <LoanTypeFormModal
        key={editingItem?.id ?? "new"}
        isOpen={isModalOpen}
        onOpenChange={(open) => {
          setIsModalOpen(open);
          if (!open) setEditingItem(null);
        }}
        editingItem={editingItem}
        defaultDisplayOrder={loanTypesList.length + 1}
        onSubmit={handleFormSubmit}
        isLoading={isLoading}
      />

      <DeleteLoanTypeModal
        item={deleteItem}
        onOpenChange={(open) => {
          if (!open) setDeleteItem(null);
        }}
        onConfirm={handleDeleteConfirm}
        isLoading={isLoading}
      />
    </div>
  );
}
