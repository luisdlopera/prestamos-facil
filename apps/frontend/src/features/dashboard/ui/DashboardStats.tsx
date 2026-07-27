"use client";

import { useEffect, useState, useMemo, useCallback } from "react";
import { Card, Button } from "@heroui/react";
import { Users, Wallet, Clock, TrendingUp, UserPlus, FilePlus } from "lucide-react";
import { fetchCustomers } from "@/features/customers/infrastructure/customers-api";
import { fetchLoans } from "@/features/loans/infrastructure/loans-api";
import { fetchLoanApplications } from "@/features/loan-applications/infrastructure/loan-applications-api";
import { fetchApprovedLoansTotal } from "@/features/reports/infrastructure/reports-api";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useNavigate } from "@/hooks/useNavigate";
import { formatCurrency } from "@/lib/formatters/currency";

export function DashboardStats() {
  const [activeLoans, setActiveLoans] = useState<number | null>(null);
  const [totalCustomers, setTotalCustomers] = useState<number | null>(null);
  const [pendingApps, setPendingApps] = useState<number | null>(null);
  const [totalApprovedAmount, setTotalApprovedAmount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  const userType = useAuthStore((s) => s.userType);
  const navigate = useNavigate();

  useEffect(() => {
    if (userType === "customer") {
      window.location.href = "/my-loans";
      return;
    }

    let cancelled = false;
    async function load() {
      try {
        const [customersRes, loansRes, appsRes, totalRes] = await Promise.all([
          fetchCustomers({ page: 0, size: 1 }),
          fetchLoans({ page: 0, size: 1 }),
          fetchLoanApplications({ page: 0, size: 1, status: "PENDING_REVIEW" }),
          fetchApprovedLoansTotal().catch(() => null),
        ]);
        if (cancelled) return;
        setTotalCustomers(customersRes.pagination?.total ?? null);
        setActiveLoans(loansRes.pagination?.total ?? null);
        setPendingApps(appsRes.pagination?.total ?? null);
        setTotalApprovedAmount(totalRes?.totalApprovedAmount ?? null);
      } catch {
        // Silently handle fallback
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [userType]);

  const handleNewCustomer = useCallback(() => {
    navigate("/register-customer");
  }, [navigate]);

  const handleNewApplication = useCallback(() => {
    navigate("/new-loan-application");
  }, [navigate]);

  const kpiCards = useMemo(
    () => [
      {
        label: "Clientes Registrados",
        value: totalCustomers !== null ? totalCustomers.toLocaleString("es-CO") : "—",
        Icon: Users,
        iconBg: "bg-blue-50 text-blue-600",
      },
      {
        label: "Préstamos Activos",
        value: activeLoans !== null ? activeLoans.toLocaleString("es-CO") : "—",
        Icon: Wallet,
        iconBg: "bg-emerald-50 text-emerald-600",
      },
      {
        label: "Solicitudes Pendientes",
        value: pendingApps !== null ? pendingApps.toLocaleString("es-CO") : "—",
        Icon: Clock,
        iconBg: "bg-amber-50 text-amber-600",
      },
      {
        label: "Total Prestado",
        value: totalApprovedAmount !== null ? formatCurrency(totalApprovedAmount) : "—",
        Icon: TrendingUp,
        iconBg: "bg-violet-50 text-violet-600",
      },
    ],
    [totalCustomers, activeLoans, pendingApps, totalApprovedAmount],
  );

  return (
    <div className="space-y-6">
      {/* Quick Action Shortcuts Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 rounded-2xl bg-gradient-to-r from-blue-900 to-indigo-800 p-6 text-white shadow-sm">
        <div>
          <h2 className="text-xl font-bold tracking-tight">Bienvenido al Panel Financiero</h2>
          <p className="mt-1 text-sm text-blue-100">
            Gestiona clientes, solicitudes y créditos con facilidad y control total.
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <Button
            size="sm"
            onPress={handleNewCustomer}
            className="bg-white font-semibold text-blue-900 shadow-xs hover:bg-blue-50"
          >
            <UserPlus className="size-4" />
            Nuevo Cliente
          </Button>
          <Button
            size="sm"
            onPress={handleNewApplication}
            className="bg-blue-600 font-semibold text-white shadow-xs hover:bg-blue-500"
          >
            <FilePlus className="size-4" />
            Nueva Solicitud
          </Button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpiCards.map(({ label, value, Icon, iconBg }) => (
          <Card
            key={label}
            className="p-5 border border-slate-200/80 bg-white rounded-xl shadow-xs hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                {label}
              </span>
              <div className={`flex size-9 items-center justify-center rounded-lg ${iconBg}`}>
                <Icon className="size-5" />
              </div>
            </div>

            <div className="mt-3">
              <p className="text-2xl font-bold tracking-tight text-slate-900">
                {loading ? (
                  <span className="text-sm font-normal text-slate-400">Cargando...</span>
                ) : (
                  value
                )}
              </p>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
