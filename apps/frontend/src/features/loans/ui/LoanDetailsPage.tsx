"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { ServerTable } from "@/components/ui/ServerTable";
import type { ServerTableColumn } from "@/components/ui/ServerTable";
import type { ServerTableParams, ServerTableResult } from "@/hooks/useServerTable";
import type { LoanDto, PaymentInstallmentDto } from "../infrastructure/loans-api";
import { fetchLoanById, fetchPaymentPlan } from "../infrastructure/loans-api";
import { useAuthStore } from "@/lib/stores/auth.store";
import { formatCurrency } from "@/lib/formatters/currency";
import {
  AlertTriangle,
  RefreshCw,
  Wallet,
  Calendar,
  Percent,
  Clock,
  CheckCircle2,
  FileSpreadsheet,
} from "lucide-react";
import { Button } from "@heroui/react";

const paymentColumns: ServerTableColumn<PaymentInstallmentDto>[] = [
  {
    key: "installmentNumber",
    header: "Cuota",
    type: "number",
    sortable: true,
    align: "center",
  },
  {
    key: "dueDate",
    header: "Fecha Vencimiento",
    type: "date",
    sortable: true,
  },
  {
    key: "openingBalance",
    header: "Saldo Inicial",
    type: "currency",
  },
  {
    key: "paymentAmount",
    header: "Cuota Mensual",
    type: "currency",
  },
  {
    key: "principalAmount",
    header: "Abono Capital",
    type: "currency",
  },
  {
    key: "interestAmount",
    header: "Interés",
    type: "currency",
  },
  {
    key: "closingBalance",
    header: "Saldo Final",
    type: "currency",
  },
];

interface LoanDetailsPageProps {
  loanId?: string;
}

export function LoanDetailsPage({ loanId: propLoanId }: LoanDetailsPageProps = {}) {
  const user = useAuthStore((s) => s.user);
  const userType = useAuthStore((s) => s.userType);
  const [loan, setLoan] = useState<LoanDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [loanId, setLoanId] = useState<string | null>(propLoanId || null);

  const allInstallmentsRef = useRef<PaymentInstallmentDto[]>([]);

  useEffect(() => {
    if (typeof window !== "undefined" && !propLoanId) {
      const params = new URLSearchParams(window.location.search);
      setLoanId(params.get("id") || params.get("loanId"));
    }
  }, [propLoanId]);

  useEffect(() => {
    if (!loanId) {
      setLoading(false);
      return;
    }

    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const loanData = await fetchLoanById(loanId);
        if (cancelled) return;

        if (loanData) {
          const isAllowed = userType === "staff" || loanData.customerId === user?.id;
          if (isAllowed) {
            setLoan(loanData);
          } else {
            setError("No tienes permiso para ver este préstamo");
          }
        } else {
          setLoan(null);
        }
      } catch (e) {
        if (!cancelled) {
          const msg = e instanceof Error ? e.message : "Error al cargar los detalles del préstamo";
          setError(msg);
          setLoan(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [loanId, user?.id, userType]);

  const fetchPlan = useCallback(
    async (params: ServerTableParams): Promise<ServerTableResult<PaymentInstallmentDto>> => {
      if (!loan?.id) {
        return {
          data: [],
          pagination: { page: 0, perPage: params.size, total: 0, totalPages: 0 },
        };
      }

      try {
        const installments = await fetchPaymentPlan(loan.id);
        allInstallmentsRef.current = installments;

        const search = params.search?.trim().toLowerCase();
        let filtered = installments;
        if (search) {
          filtered = installments.filter((inst) => {
            const searchStr =
              `${inst.installmentNumber} ${inst.dueDate} ${inst.openingBalance} ${inst.paymentAmount} ${inst.principalAmount} ${inst.interestAmount} ${inst.closingBalance}`.toLowerCase();
            return searchStr.includes(search);
          });
        }

        const page = params.page;
        const size = params.size;
        const start = page * size;
        const paginatedData = filtered.slice(start, start + size);

        return {
          data: paginatedData,
          pagination: {
            page,
            perPage: size,
            total: filtered.length,
            totalPages: Math.max(1, Math.ceil(filtered.length / size)),
          },
        };
      } catch {
        return {
          data: [],
          pagination: { page: 0, perPage: params.size, total: 0, totalPages: 0 },
        };
      }
    },
    [loan?.id],
  );

  if (loading) {
    return (
      <div className="flex h-48 items-center justify-center text-sm font-medium text-slate-500">
        Cargando detalles del préstamo...
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-48 flex-col items-center justify-center gap-3 text-sm font-medium text-slate-500">
        <AlertTriangle className="size-8 text-red-400" />
        <span className="text-red-600">{error}</span>
        <Button
          size="sm"
          variant="ghost"
          onPress={() => {
            setLoanId(loanId);
          }}
        >
          <RefreshCw className="size-4" />
          Reintentar
        </Button>
      </div>
    );
  }

  if (!loan) {
    return (
      <div className="flex h-48 items-center justify-center text-sm font-medium text-slate-500">
        Préstamo no encontrado
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header Info Card */}
      <div className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4 mb-6">
          <div className="flex items-center gap-3">
            <div className="flex size-10 items-center justify-center rounded-xl bg-blue-50 text-blue-600 border border-blue-100">
              <Wallet className="size-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">Detalles del Préstamo</h2>
              <p className="text-xs text-slate-500">
                Información del crédito y cronograma de amortización
              </p>
            </div>
          </div>

          <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="size-3.5" />
            Aprobado
          </span>
        </div>

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <Wallet className="size-3.5 text-blue-600" />
              <span>Monto Capital</span>
            </div>
            <p className="text-base font-bold text-slate-900">
              {formatCurrency(loan.principalAmount)}
            </p>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <Percent className="size-3.5 text-blue-600" />
              <span>Tasa Anual</span>
            </div>
            <p className="text-base font-bold text-slate-900">{loan.annualInterestRate}% anual</p>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <Clock className="size-3.5 text-blue-600" />
              <span>Plazo</span>
            </div>
            <p className="text-base font-bold text-slate-900">{loan.termInMonths} meses</p>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <Wallet className="size-3.5 text-emerald-600" />
              <span>Cuota Mensual</span>
            </div>
            <p className="text-base font-bold text-emerald-700">
              {formatCurrency(loan.monthlyPayment)}
            </p>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <Calendar className="size-3.5 text-blue-600" />
              <span>Fecha Aprobación</span>
            </div>
            <p className="text-sm font-semibold text-slate-800">
              {loan.approvedAt ? new Date(loan.approvedAt).toLocaleDateString("es-CO") : "N/A"}
            </p>
          </div>

          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 mb-1">
              <FileSpreadsheet className="size-3.5 text-blue-600" />
              <span>Cuotas Totales</span>
            </div>
            <p className="text-base font-bold text-slate-900">{loan.termInMonths} cuotas</p>
          </div>
        </div>
      </div>

      {/* Plan de Pagos Card */}
      <div className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm">
        <div className="flex items-center gap-2 mb-4">
          <FileSpreadsheet className="size-5 text-blue-600" />
          <h3 className="text-base font-bold text-slate-900">Plan de Pagos</h3>
        </div>
        <ServerTable<PaymentInstallmentDto>
          columns={paymentColumns}
          fetchFn={fetchPlan}
          emptyMessage="No hay plan de pagos disponible para este préstamo"
        />
      </div>
    </div>
  );
}
