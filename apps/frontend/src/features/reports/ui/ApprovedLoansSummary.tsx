"use client";

import { useEffect, useState, useCallback, useMemo } from "react";
import { Card, Button, DateRangePicker, RangeCalendar, DateField, Spinner } from "@heroui/react";
import { parseDate, type DateValue } from "@internationalized/date";
import {
  BadgeDollarSign,
  ListOrdered,
  ShieldAlert,
  FileSpreadsheet,
  FileText,
  Filter,
  PieChart,
  BarChart3,
  TrendingUp,
  Percent,
  Lock,
} from "lucide-react";
import { fetchApprovedLoansTotal } from "../infrastructure/reports-api";
import type { ApprovedLoansTotalDto } from "../infrastructure/reports-api";
import { fetchLoanApplications } from "@/features/loan-applications/infrastructure/loan-applications-api";
import type { LoanApplicationDto } from "@/features/loan-applications/infrastructure/loan-applications-api";
import { fetchLoans } from "@/features/loans/infrastructure/loans-api";
import type { LoanDto } from "@/features/loans/infrastructure/loans-api";
import { formatCurrency } from "@/lib/formatters/currency";
import { useAuthStore, checkIsAdmin } from "@/lib/stores/auth.store";

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

export function ApprovedLoansSummary() {
  const [data, setData] = useState<ApprovedLoansTotalDto | null>(null);
  const [applications, setApplications] = useState<LoanApplicationDto[]>([]);
  const [loans, setLoans] = useState<LoanDto[]>([]);
  const [startDate, setStartDate] = useState<string>("2026-01-01");
  const [endDate, setEndDate] = useState<string>("2026-12-31");
  const [dateRange, setDateRange] = useState<{ start: DateValue; end: DateValue } | null>({
    start: parseDate("2026-01-01"),
    end: parseDate("2026-12-31"),
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const user = useAuthStore((s) => s.user);
  const userType = useAuthStore((s) => s.userType);
  const isAuthLoading = useAuthStore((s) => s.isLoading);

  const isAdmin = useMemo(() => checkIsAdmin(user, userType), [user, userType]);

  const loadRealReportData = useCallback(async () => {
    if (!isAdmin) return;
    setLoading(true);
    setError(null);

    try {
      const [totalRes, appsRes, loansRes] = await Promise.all([
        fetchApprovedLoansTotal().catch(() => null),
        fetchLoanApplications({ page: 0, size: 100 }).catch(() => ({ data: [] })),
        fetchLoans({ page: 0, size: 100 }).catch(() => ({ data: [] })),
      ]);

      if (totalRes) setData(totalRes);
      setApplications(appsRes.data ?? []);
      setLoans(loansRes.data ?? []);
    } catch {
      setError("Error al cargar los datos de reportes. Intente de nuevo más tarde.");
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  useEffect(() => {
    loadRealReportData();
  }, [loadRealReportData]);

  // Real Export to PDF/TXT containing real record items
  const handleExportPDF = useCallback(() => {
    let reportText = `=====================================================\n`;
    reportText += `       REPORTE FINANCIERO DE PRÉSTAMOS REALES        \n`;
    reportText += `=====================================================\n`;
    reportText += `Generado: ${new Date().toLocaleString("es-CO")}\n`;
    reportText += `Rango de Fechas: ${startDate} a ${endDate}\n\n`;
    reportText += `RESUMEN GENERAL:\n`;
    reportText += `- Total Monto Aprobado: ${data ? formatCurrency(data.totalApprovedAmount) : "$0"}\n`;
    reportText += `- Cantidad de Préstamos: ${data ? data.approvedLoansCount : 0}\n\n`;
    reportText += `DETALLE DE SOLICITUDES (${applications.length} registros):\n`;

    applications.forEach((app, idx) => {
      reportText += `${idx + 1}. [${app.status}] Cliente: ${app.customerName || app.customerEmail} | Monto: ${formatCurrency(app.requestedAmount)} | Tipo: ${app.loanTypeName} | Fecha: ${new Date(app.createdAt).toLocaleDateString("es-CO")}\n`;
    });

    const blob = new Blob([reportText], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `reporte_financiero_real_${new Date().toISOString().slice(0, 10)}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  }, [data, applications, startDate, endDate]);

  // Real Export to Excel / CSV with real rows
  const handleExportExcel = useCallback(() => {
    let csv = `ID,Cliente,Correo,Tipo Prestamo,Monto Solicitado,Estado,Fecha Solicitud\n`;
    applications.forEach((app) => {
      csv += `"${app.id}","${app.customerName}","${app.customerEmail}","${app.loanTypeName}",${app.requestedAmount},"${app.status}","${app.createdAt}"\n`;
    });

    const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `reporte_solicitudes_reales_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }, [applications]);

  // Real dynamic Calculations
  const averageLoanAmount = useMemo(() => {
    if (!data || data.approvedLoansCount === 0) return 0;
    return Math.round(data.totalApprovedAmount / data.approvedLoansCount);
  }, [data]);

  const realApprovalRate = useMemo(() => {
    if (applications.length === 0) return 0;
    const approvedCount = applications.filter(
      (a) => a.status === "APPROVED" || a.status === "ACTIVE",
    ).length;
    return Math.round((approvedCount / applications.length) * 1000) / 10;
  }, [applications]);

  // Real monthly totals computed from database records
  const realMonthlyStats = useMemo(() => {
    const monthsList: { month: string; amountMillions: number; count: number }[] = [];
    const now = new Date();

    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const label = MONTH_NAMES[d.getMonth()];
      const year = d.getFullYear();
      const monthIdx = d.getMonth();

      const monthLoans = loans.filter((loan) => {
        const dt = new Date(loan.approvedAt);
        return dt.getFullYear() === year && dt.getMonth() === monthIdx;
      });

      const totalSum = monthLoans.reduce((acc, l) => acc + (l.principalAmount || 0), 0);
      const millions = Math.round((totalSum / 1_000_000) * 10) / 10;

      monthsList.push({
        month: label,
        amountMillions: millions,
        count: monthLoans.length,
      });
    }

    return monthsList;
  }, [loans]);

  const maxMonthlyAmount = useMemo(() => {
    const max = Math.max(...realMonthlyStats.map((m) => m.amountMillions));
    return max > 0 ? max : 1;
  }, [realMonthlyStats]);

  const maxMonthlyCount = useMemo(() => {
    const max = Math.max(...realMonthlyStats.map((m) => m.count));
    return max > 0 ? max : 1;
  }, [realMonthlyStats]);

  // Real Loan Type Distribution
  const realLoanTypeDistribution = useMemo(() => {
    if (applications.length === 0) return [];
    const counts: Record<string, number> = {};

    applications.forEach((app) => {
      const name = app.loanTypeName || "General";
      counts[name] = (counts[name] || 0) + 1;
    });

    const colors = ["bg-blue-600", "bg-emerald-500", "bg-violet-500", "bg-amber-500"];
    const textColors = ["text-blue-700", "text-emerald-700", "text-violet-700", "text-amber-700"];

    return Object.entries(counts).map(([name, count], idx) => {
      const pct = Math.round((count / applications.length) * 100);
      return {
        name,
        pct,
        count,
        color: colors[idx % colors.length],
        text: textColors[idx % textColors.length],
      };
    });
  }, [applications]);

  // Real Status Breakdown
  const realStatusBreakdown = useMemo(() => {
    if (applications.length === 0) return [];
    const total = applications.length;

    const approved = applications.filter(
      (a) => a.status === "APPROVED" || a.status === "ACTIVE",
    ).length;
    const pending = applications.filter((a) => a.status === "PENDING_REVIEW").length;
    const rejected = applications.filter((a) => a.status === "REJECTED").length;

    return [
      {
        name: "Aprobadas",
        count: approved,
        pct: Math.round((approved / total) * 100),
        color: "bg-emerald-500",
        text: "text-emerald-700",
      },
      {
        name: "Pendientes de Revisión",
        count: pending,
        pct: Math.round((pending / total) * 100),
        color: "bg-amber-500",
        text: "text-amber-700",
      },
      {
        name: "Rechazadas",
        count: rejected,
        pct: Math.round((rejected / total) * 100),
        color: "bg-rose-500",
        text: "text-rose-700",
      },
    ];
  }, [applications]);

  if (isAuthLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <Card className="w-full p-8 text-center border border-slate-200 bg-white rounded-2xl">
        <div className="flex flex-col items-center justify-center space-y-3">
          <div className="flex size-12 items-center justify-center rounded-2xl bg-rose-50 text-rose-600">
            <ShieldAlert className="size-6" />
          </div>
          <h2 className="text-lg font-bold text-slate-900">Error al Cargar Reportes</h2>
          <p className="text-xs text-slate-500 max-w-md">{error}</p>
          <Button
            size="sm"
            onPress={loadRealReportData}
            className="mt-2 bg-blue-600 text-white font-semibold"
          >
            Reintentar
          </Button>
        </div>
      </Card>
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
            Los reportes y análisis ejecutivos solo están disponibles para usuarios con rol
            Administrador.
          </p>
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Date Filter Bar & Export Actions */}
      <Card className="p-4 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div
            className="relative flex flex-wrap items-center gap-3 w-full md:w-auto opacity-60 cursor-not-allowed select-none"
            title="Función disponible en un futuro"
          >
            <button
              type="button"
              className="absolute inset-0 z-10 cursor-not-allowed w-full h-full bg-transparent border-none p-0"
              onClick={(e) => {
                e.stopPropagation();
                alert("Función disponible en un futuro");
              }}
              title="Función disponible en un futuro"
              aria-label="Función disponible en un futuro"
            />
            <div className="flex items-center gap-2 text-xs font-bold text-slate-700">
              <Filter className="size-4 text-blue-600" />
              <span>Rango de Fechas:</span>
              <span className="inline-flex items-center gap-1 text-[10px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded-full font-normal">
                <Lock className="size-3 text-slate-400" />
                Próximamente
              </span>
            </div>
            <DateRangePicker
              isDisabled
              value={dateRange}
              onChange={(val) => {
                if (val) {
                  setDateRange(val);
                  setStartDate(val.start.toString());
                  setEndDate(val.end.toString());
                } else {
                  setDateRange(null);
                }
              }}
              aria-label="Rango de Fechas"
              className="w-72 pointer-events-none"
            >
              <DateField.Group fullWidth className="h-9 text-xs">
                <DateField.Input slot="start">
                  {(segment) => <DateField.Segment segment={segment} />}
                </DateField.Input>
                <DateRangePicker.RangeSeparator />
                <DateField.Input slot="end">
                  {(segment) => <DateField.Segment segment={segment} />}
                </DateField.Input>
                <DateField.Suffix>
                  <DateRangePicker.Trigger>
                    <DateRangePicker.TriggerIndicator />
                  </DateRangePicker.Trigger>
                </DateField.Suffix>
              </DateField.Group>
              <DateRangePicker.Popover>
                <RangeCalendar aria-label="Rango de Fechas">
                  <RangeCalendar.Header>
                    <RangeCalendar.YearPickerTrigger>
                      <RangeCalendar.YearPickerTriggerHeading />
                      <RangeCalendar.YearPickerTriggerIndicator />
                    </RangeCalendar.YearPickerTrigger>
                    <RangeCalendar.NavButton slot="previous" />
                    <RangeCalendar.NavButton slot="next" />
                  </RangeCalendar.Header>
                  <RangeCalendar.Grid>
                    <RangeCalendar.GridHeader>
                      {(day) => <RangeCalendar.HeaderCell>{day}</RangeCalendar.HeaderCell>}
                    </RangeCalendar.GridHeader>
                    <RangeCalendar.GridBody>
                      {(date) => <RangeCalendar.Cell date={date} />}
                    </RangeCalendar.GridBody>
                  </RangeCalendar.Grid>
                  <RangeCalendar.YearPickerGrid>
                    <RangeCalendar.YearPickerGridBody>
                      {({ year }) => <RangeCalendar.YearPickerCell year={year} />}
                    </RangeCalendar.YearPickerGridBody>
                  </RangeCalendar.YearPickerGrid>
                </RangeCalendar>
              </DateRangePicker.Popover>
            </DateRangePicker>
            <Button
              isDisabled
              size="sm"
              onPress={() => alert("Función disponible en un futuro")}
              className="h-9 bg-blue-600 text-white font-semibold shadow-xs opacity-70 pointer-events-none"
            >
              Aplicar Filtro
            </Button>
          </div>

          <div className="flex items-center gap-2 shrink-0 w-full md:w-auto justify-end">
            <Button
              size="sm"
              variant="tertiary"
              onPress={handleExportPDF}
              className="h-9 border border-slate-200 font-semibold text-slate-700 hover:bg-slate-50"
            >
              <FileText className="size-4 text-rose-600" />
              Exportar PDF / TXT
            </Button>
            <Button
              size="sm"
              variant="tertiary"
              onPress={handleExportExcel}
              className="h-9 border border-slate-200 font-semibold text-slate-700 hover:bg-slate-50"
            >
              <FileSpreadsheet className="size-4 text-emerald-600" />
              Exportar Excel CSV
            </Button>
          </div>
        </div>
      </Card>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-5 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Total Aprobado
            </span>
            <div className="flex size-9 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
              <BadgeDollarSign className="size-5" />
            </div>
          </div>
          <p className="mt-3 text-2xl font-bold text-blue-600">
            {loading ? "Cargando..." : data ? formatCurrency(data.totalApprovedAmount) : "$0"}
          </p>
          <span className="mt-1 block text-xs font-medium text-slate-500">
            Monto total concedido en el sistema
          </span>
        </Card>

        <Card className="p-5 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Cantidad de Préstamos
            </span>
            <div className="flex size-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
              <ListOrdered className="size-5" />
            </div>
          </div>
          <p className="mt-3 text-2xl font-bold text-slate-900">
            {loading ? "Cargando..." : data ? data.approvedLoansCount.toLocaleString("es-CO") : "0"}
          </p>
          <span className="mt-1 block text-xs font-medium text-slate-500">
            Préstamos generados exitosamente
          </span>
        </Card>

        <Card className="p-5 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Promedio por Préstamo
            </span>
            <div className="flex size-9 items-center justify-center rounded-lg bg-violet-50 text-violet-600">
              <TrendingUp className="size-5" />
            </div>
          </div>
          <p className="mt-3 text-2xl font-bold text-slate-900">
            {loading ? "Cargando..." : formatCurrency(averageLoanAmount)}
          </p>
          <span className="mt-1 block text-xs font-medium text-slate-500">
            Ticket promedio de crédito
          </span>
        </Card>

        <Card className="p-5 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Tasa de Aprobación
            </span>
            <div className="flex size-9 items-center justify-center rounded-lg bg-amber-50 text-amber-600">
              <Percent className="size-5" />
            </div>
          </div>
          <p className="mt-3 text-2xl font-bold text-slate-900">
            {loading ? "Cargando..." : `${realApprovalRate}%`}
          </p>
          <span className="mt-1 block text-xs font-medium text-emerald-600">
            Calculado sobre {applications.length} solicitudes
          </span>
        </Card>
      </div>

      {/* Charts Grid (Real Calculations) */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Chart 1: Total Aprobado por Mes (Real) */}
        <Card className="p-6 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <BarChart3 className="size-5 text-blue-600" />
            <div>
              <h3 className="text-base font-bold text-slate-900">Total Aprobado por Mes</h3>
              <p className="text-xs text-slate-500">
                Monto total mensual en millones COP (Base de Datos)
              </p>
            </div>
          </div>
          <div className="h-52 flex items-end justify-between gap-3 pt-6 border-b border-slate-100">
            {realMonthlyStats.map((d) => {
              const heightPct = Math.round((d.amountMillions / maxMonthlyAmount) * 100);
              return (
                <div
                  key={d.month}
                  className="flex-1 flex flex-col items-center gap-2 h-full justify-end group"
                >
                  <div
                    style={{ height: `${d.amountMillions > 0 ? Math.max(8, heightPct) : 0}%` }}
                    className={`w-full max-w-[28px] rounded-t-md transition-all group-hover:brightness-110 relative ${
                      d.amountMillions > 0
                        ? "bg-gradient-to-t from-blue-600 to-indigo-500"
                        : "bg-slate-100"
                    }`}
                  >
                    {d.amountMillions > 0 && (
                      <span className="opacity-0 group-hover:opacity-100 transition-opacity absolute -top-7 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[10px] font-bold px-1.5 py-0.5 rounded shadow-sm whitespace-nowrap z-10">
                        ${d.amountMillions}M
                      </span>
                    )}
                  </div>
                  <span className="text-xs font-semibold text-slate-500">{d.month}</span>
                </div>
              );
            })}
          </div>
        </Card>

        {/* Chart 2: Cantidad de Préstamos por Mes (Real) */}
        <Card className="p-6 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <ListOrdered className="size-5 text-emerald-600" />
            <div>
              <h3 className="text-base font-bold text-slate-900">Cantidad de Préstamos</h3>
              <p className="text-xs text-slate-500">
                Número de préstamos otorgados por mes (Base de Datos)
              </p>
            </div>
          </div>
          <div className="h-52 flex items-end justify-between gap-3 pt-6 border-b border-slate-100">
            {realMonthlyStats.map((d) => {
              const heightPct = Math.round((d.count / maxMonthlyCount) * 100);
              return (
                <div
                  key={d.month}
                  className="flex-1 flex flex-col items-center gap-2 h-full justify-end group"
                >
                  <div
                    style={{ height: `${d.count > 0 ? Math.max(8, heightPct) : 0}%` }}
                    className={`w-full max-w-[28px] rounded-t-md transition-all group-hover:brightness-110 relative ${
                      d.count > 0 ? "bg-gradient-to-t from-emerald-500 to-teal-400" : "bg-slate-100"
                    }`}
                  >
                    {d.count > 0 && (
                      <span className="opacity-0 group-hover:opacity-100 transition-opacity absolute -top-7 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[10px] font-bold px-1.5 py-0.5 rounded shadow-sm whitespace-nowrap z-10">
                        {d.count} u
                      </span>
                    )}
                  </div>
                  <span className="text-xs font-semibold text-slate-500">{d.month}</span>
                </div>
              );
            })}
          </div>
        </Card>

        {/* Chart 3: Distribución por Tipo de Préstamo (Real) */}
        <Card className="p-6 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <PieChart className="size-5 text-violet-600" />
            <div>
              <h3 className="text-base font-bold text-slate-900">
                Distribución por Tipo de Préstamo
              </h3>
              <p className="text-xs text-slate-500">Participación real por línea de crédito</p>
            </div>
          </div>
          {realLoanTypeDistribution.length === 0 ? (
            <div className="h-40 flex items-center justify-center text-xs text-slate-400">
              No hay solicitudes registradas
            </div>
          ) : (
            <div className="space-y-4 pt-2">
              {realLoanTypeDistribution.map((type) => (
                <div key={type.name} className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-semibold">
                    <span className="text-slate-800">
                      {type.name} ({type.count} solicitudes)
                    </span>
                    <span className={type.text}>{type.pct}%</span>
                  </div>
                  <div className="h-3 w-full rounded-full bg-slate-100 overflow-hidden">
                    <div
                      style={{ width: `${type.pct}%` }}
                      className={`h-full ${type.color} rounded-full`}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Chart 4: Estados de Solicitudes (Real) */}
        <Card className="p-6 border border-slate-200/80 bg-white rounded-2xl shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="size-5 text-amber-600" />
            <div>
              <h3 className="text-base font-bold text-slate-900">Estados de Solicitudes</h3>
              <p className="text-xs text-slate-500">
                Desglose real por estado actual de aprobación
              </p>
            </div>
          </div>
          {applications.length === 0 ? (
            <div className="h-40 flex items-center justify-center text-xs text-slate-400">
              No hay solicitudes registradas
            </div>
          ) : (
            <div className="space-y-4 pt-2">
              {realStatusBreakdown.map((st) => (
                <div key={st.name} className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-semibold">
                    <span className="text-slate-800">
                      {st.name} ({st.count} solicitudes)
                    </span>
                    <span className={st.text}>{st.pct}%</span>
                  </div>
                  <div className="h-3 w-full rounded-full bg-slate-100 overflow-hidden">
                    <div
                      style={{ width: `${st.pct}%` }}
                      className={`h-full ${st.color} rounded-full`}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
