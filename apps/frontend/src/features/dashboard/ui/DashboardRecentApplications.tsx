"use client";

import { useEffect, useState, useCallback } from "react";
import { Card, Button } from "@heroui/react";
import { FileText, ArrowRight, Clock, CheckCircle2, XCircle } from "lucide-react";
import { fetchLoanApplications } from "@/features/loan-applications/infrastructure/loan-applications-api";
import type { LoanApplicationDto } from "@/features/loan-applications/infrastructure/loan-applications-api";
import { formatCurrency } from "@/lib/formatters/currency";
import { formatDate } from "@/lib/formatters/date";
import { useNavigate } from "@/hooks/useNavigate";

export function DashboardRecentApplications() {
  const [applications, setApplications] = useState<LoanApplicationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const res = await fetchLoanApplications({ page: 0, size: 5 });
        if (active && res.data) {
          setApplications(res.data);
        }
      } catch {
        // Silently handle error
      } finally {
        if (active) setLoading(false);
      }
    }
    load();
    return () => {
      active = false;
    };
  }, []);

  const handleViewAll = useCallback(() => {
    navigate("/loan-applications");
  }, [navigate]);

  const renderStatusBadge = useCallback((status: string) => {
    switch (status) {
      case "APPROVED":
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="size-3" />
            Aprobada
          </span>
        );
      case "REJECTED":
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200">
            <XCircle className="size-3" />
            Rechazada
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700 border border-amber-200">
            <Clock className="size-3" />
            Pendiente
          </span>
        );
    }
  }, []);

  return (
    <Card className="p-6 border border-slate-200/80 bg-white rounded-xl shadow-xs">
      <div className="flex items-center justify-between gap-2 mb-4">
        <div>
          <h3 className="text-base font-bold text-slate-900">Solicitudes Recientes</h3>
          <p className="text-xs text-slate-500">Últimas solicitudes recibidas en la plataforma</p>
        </div>
        <Button
          size="sm"
          variant="tertiary"
          onPress={handleViewAll}
          className="text-xs font-semibold text-blue-600 hover:text-blue-700 hover:bg-blue-50"
        >
          Ver Todas
          <ArrowRight className="size-3.5" />
        </Button>
      </div>

      {loading ? (
        <div className="py-8 text-center text-xs text-slate-400">Cargando solicitudes...</div>
      ) : applications.length === 0 ? (
        <div className="py-8 text-center text-xs text-slate-400">No hay solicitudes recientes</div>
      ) : (
        <div className="divide-y divide-slate-100">
          {applications.map((app) => (
            <div
              key={app.id}
              className="py-3.5 flex items-center justify-between gap-4 first:pt-0 last:pb-0 hover:bg-slate-50/60 rounded-lg px-2 transition-colors"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div className="flex size-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600 shrink-0">
                  <FileText className="size-4" />
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-slate-900 truncate">
                    {app.customerName || app.customerEmail}
                  </p>
                  <p className="text-xs text-slate-500 truncate">
                    {app.loanTypeName} &bull; {app.termInMonths} meses
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-4 shrink-0 text-right">
                <div>
                  <p className="text-sm font-bold text-slate-900">
                    {formatCurrency(app.requestedAmount)}
                  </p>
                  <p className="text-[11px] text-slate-400">{formatDate(app.createdAt)}</p>
                </div>
                <div>{renderStatusBadge(app.status)}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}
