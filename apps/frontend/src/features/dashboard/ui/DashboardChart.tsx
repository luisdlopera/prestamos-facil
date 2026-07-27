"use client";

import { useEffect, useState, useMemo } from "react";
import { Card } from "@heroui/react";
import { fetchLoanApplications } from "@/features/loan-applications/infrastructure/loan-applications-api";
import { fetchLoans } from "@/features/loans/infrastructure/loans-api";

interface MonthlyData {
  month: string;
  solicitudes: number;
  aprobados: number;
}

const MONTH_NAMES = [
  "Ene",
  "Feb",
  "Mar",
  "Abr",
  "May",
  "Jun",
  "Jul",
  "Ago",
  "Sep",
  "Oct",
  "Nov",
  "Dic",
];

export function DashboardChart() {
  const [monthlyData, setMonthlyData] = useState<MonthlyData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadRealData() {
      try {
        const [appsRes, loansRes] = await Promise.all([
          fetchLoanApplications({ page: 0, size: 100 }).catch(() => ({ data: [] })),
          fetchLoans({ page: 0, size: 100 }).catch(() => ({ data: [] })),
        ]);

        if (!active) return;

        const apps = appsRes.data ?? [];
        const loans = loansRes.data ?? [];

        // Build last 6 months list dynamically based on current date
        const resultMonths: MonthlyData[] = [];
        const now = new Date();

        for (let i = 5; i >= 0; i--) {
          const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthLabel = MONTH_NAMES[d.getMonth()];
          const year = d.getFullYear();
          const monthIndex = d.getMonth();

          const monthApps = apps.filter((app) => {
            if (!app.createdAt) return false;
            const appDate = new Date(app.createdAt);
            return appDate.getFullYear() === year && appDate.getMonth() === monthIndex;
          });

          const monthLoans = loans.filter((loan) => {
            const dateStr = loan.approvedAt;
            if (!dateStr) return false;
            const loanDate = new Date(dateStr);
            return loanDate.getFullYear() === year && loanDate.getMonth() === monthIndex;
          });

          resultMonths.push({
            month: monthLabel,
            solicitudes: monthApps.length,
            aprobados: monthLoans.length,
          });
        }

        setMonthlyData(resultMonths);
      } catch {
        if (active) setMonthlyData([]);
      } finally {
        if (active) setLoading(false);
      }
    }

    loadRealData();

    return () => {
      active = false;
    };
  }, []);

  const maxVal = useMemo(() => {
    if (monthlyData.length === 0) return 1;
    const max = Math.max(...monthlyData.map((d) => Math.max(d.solicitudes, d.aprobados)));
    return max > 0 ? max : 1;
  }, [monthlyData]);

  return (
    <Card className="p-6 border border-slate-200/80 bg-white rounded-xl shadow-xs">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-6">
        <div>
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold text-slate-900">Préstamos por Mes (Datos Reales)</h3>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Comparativo real de solicitudes recibidas vs aprobadas en los últimos 6 meses
          </p>
        </div>
        <div className="flex items-center gap-4 text-xs font-medium text-slate-600">
          <div className="flex items-center gap-1.5">
            <span className="size-3 rounded-sm bg-blue-600"></span>
            <span>Solicitudes</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="size-3 rounded-sm bg-emerald-500"></span>
            <span>Aprobadas</span>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="h-56 flex items-center justify-center text-xs text-slate-400">
          Cargando métricas reales...
        </div>
      ) : monthlyData.length === 0 ? (
        <div className="h-56 flex items-center justify-center text-xs text-slate-400">
          No hay solicitudes ni préstamos registrados
        </div>
      ) : (
        <div className="w-full h-56 flex items-end justify-between gap-2 pt-4 px-2 border-b border-slate-100">
          {monthlyData.map((item) => {
            const solPct = Math.round((item.solicitudes / maxVal) * 100);
            const aprPct = Math.round((item.aprobados / maxVal) * 100);

            return (
              <div
                key={item.month}
                className="flex-1 flex flex-col items-center gap-2 h-full justify-end group"
              >
                <div className="w-full flex items-end justify-center gap-1 h-full px-1">
                  {/* Bar Solicitudes */}
                  <div
                    style={{ height: `${item.solicitudes > 0 ? Math.max(8, solPct) : 0}%` }}
                    className={`w-1/2 max-w-[20px] rounded-t-sm transition-all duration-300 relative ${
                      item.solicitudes > 0 ? "bg-blue-600 group-hover:bg-blue-700" : "bg-slate-100"
                    }`}
                    title={`Solicitudes: ${item.solicitudes}`}
                  >
                    {item.solicitudes > 0 && (
                      <span className="opacity-0 group-hover:opacity-100 transition-opacity absolute -top-7 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[10px] font-medium px-1.5 py-0.5 rounded shadow-sm whitespace-nowrap z-10">
                        {item.solicitudes}
                      </span>
                    )}
                  </div>
                  {/* Bar Aprobados */}
                  <div
                    style={{ height: `${item.aprobados > 0 ? Math.max(8, aprPct) : 0}%` }}
                    className={`w-1/2 max-w-[20px] rounded-t-sm transition-all duration-300 relative ${
                      item.aprobados > 0
                        ? "bg-emerald-500 group-hover:bg-emerald-600"
                        : "bg-slate-100"
                    }`}
                    title={`Aprobados: ${item.aprobados}`}
                  >
                    {item.aprobados > 0 && (
                      <span className="opacity-0 group-hover:opacity-100 transition-opacity absolute -top-7 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[10px] font-medium px-1.5 py-0.5 rounded shadow-sm whitespace-nowrap z-10">
                        {item.aprobados}
                      </span>
                    )}
                  </div>
                </div>
                <span className="text-xs font-semibold text-slate-500 pt-1">{item.month}</span>
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}
