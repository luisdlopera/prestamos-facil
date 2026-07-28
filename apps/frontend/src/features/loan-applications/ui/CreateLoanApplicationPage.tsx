"use client";

import { useEffect, useState, useMemo, useCallback } from "react";
import {
  Autocomplete,
  Button,
  Card,
  EmptyState,
  Input,
  Label,
  ListBox,
  SearchField,
  Select,
  TextField,
  useFilter,
} from "@heroui/react";
import {
  FilePlus,
  RotateCcw,
  User,
  Wallet,
  Calculator,
  CheckCircle2,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loanApplicationSchema } from "@/lib/schemas";
import type { LoanApplicationFormData } from "@/lib/schemas";
import { createLoanApplication } from "../infrastructure/loan-applications-api";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";
import { fetchCustomers } from "@/features/customers/infrastructure/customers-api";
import { fetchLoanTypes } from "@/lib/api/loan-types-api";
import type { CustomerDto } from "@/features/customers/infrastructure/customers-api";
import type { LoanTypeDto } from "@/lib/api/loan-types-api";
import { useAsyncAction, errorService } from "@/lib/errors";
import { TypedController } from "@/components/ui/TypedController";
import { useAuthStore } from "@/lib/stores/auth.store";
import { formatCurrency } from "@/lib/formatters/currency";
import { LoanApplicationResultModal } from "./LoanApplicationResultModal";
import { LoanTypeTermsNotice } from "./components/LoanTypeTermsNotice";

export function CreateLoanApplicationPage() {
  const { contains } = useFilter({ sensitivity: "base" });
  const userType = useAuthStore((s) => s.userType);
  const userId = useAuthStore((s) => s.user?.id);
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [loanTypes, setLoanTypes] = useState<LoanTypeDto[]>([]);

  const { control, handleSubmit, reset, watch, setValue, setError } = useForm<LoanApplicationFormData>({
    resolver: zodResolver(loanApplicationSchema),
    defaultValues: {
      customerId: "",
      loanTypeId: "",
      requestedAmount: 0,
      termInMonths: 0,
    },
  });

  const [applicationResult, setApplicationResult] = useState<LoanApplicationDto | null>(null);
  const [showResultModal, setShowResultModal] = useState(false);

  const selectedLoanTypeId = watch("loanTypeId");
  const requestedAmount = watch("requestedAmount");
  const termInMonths = watch("termInMonths");

  useEffect(() => {
    fetchCustomers({ page: 0, size: 100 }).then((res) => {
      const data = res.data ?? [];
      setCustomers(data);

      if (typeof window !== "undefined") {
        const params = new URLSearchParams(window.location.search);
        const paramCustomerId = params.get("customerId");
        if (paramCustomerId && data.some((c) => c.id === paramCustomerId)) {
          setValue("customerId", paramCustomerId);
        }
      }
    });

    fetchLoanTypes().then(setLoanTypes);
  }, [setValue]);

  useEffect(() => {
    if (userType === "customer" && userId) {
      setValue("customerId", userId);
    }
  }, [userType, userId, setValue]);

  const selectedLoanType = useMemo(
    () => loanTypes.find((lt) => lt.id === selectedLoanTypeId),
    [loanTypes, selectedLoanTypeId],
  );

  // Calculated estimated loan amortization preview
  const estimatedSummary = useMemo(() => {
    if (
      !selectedLoanType ||
      !requestedAmount ||
      !termInMonths ||
      requestedAmount <= 0 ||
      termInMonths <= 0
    ) {
      return null;
    }
    const rate = selectedLoanType.interestRate ?? 0;
    const rateType = selectedLoanType.rateType ?? "EA";
    // La API recibe una tasa nominal anual expresada como porcentaje.
    // La cuota definitiva siempre la recalcula el backend.
    const monthlyRate = rate > 0 ? rate / 100 / 12 : 0;
    const estimatedMonthly =
      monthlyRate > 0
        ? (requestedAmount * (monthlyRate * Math.pow(1 + monthlyRate, termInMonths))) /
          (Math.pow(1 + monthlyRate, termInMonths) - 1)
        : requestedAmount / termInMonths;
    const totalPayment = estimatedMonthly * termInMonths;
    const totalInterest = Math.max(0, totalPayment - requestedAmount);

    return {
      monthlyPayment: Math.round(estimatedMonthly),
      totalPayment: Math.round(totalPayment),
      totalInterest: Math.round(totalInterest),
      annualRate: rate,
      rateType,
    };
  }, [selectedLoanType, requestedAmount, termInMonths]);

  const { run, isLoading } = useAsyncAction<LoanApplicationDto>({
    onSuccess: (result) => {
      if (
        result.status === "APPROVED" ||
        result.status === "REJECTED" ||
        result.status === "MANUAL_REVIEW"
      ) {
        setApplicationResult(result);
        setShowResultModal(true);
      } else {
        errorService.toast.success("Solicitud creada exitosamente");
        reset();
      }
    },
  });

  const onSubmit = useCallback(
    (data: LoanApplicationFormData) => {
      if (selectedLoanType) {
        let hasValidationError = false;

        if (data.requestedAmount < selectedLoanType.minAmount) {
          setError("requestedAmount", {
            message: `El monto mínimo permitido es ${formatCurrency(selectedLoanType.minAmount)}`,
          });
          hasValidationError = true;
        } else if (data.requestedAmount > selectedLoanType.maxAmount) {
          setError("requestedAmount", {
            message: `El monto máximo permitido es ${formatCurrency(selectedLoanType.maxAmount)}`,
          });
          hasValidationError = true;
        }

        if (data.termInMonths < selectedLoanType.minTermMonths) {
          setError("termInMonths", {
            message: `El plazo mínimo permitido es ${selectedLoanType.minTermMonths} meses`,
          });
          hasValidationError = true;
        } else if (data.termInMonths > selectedLoanType.maxTermMonths) {
          setError("termInMonths", {
            message: `El plazo máximo permitido es ${selectedLoanType.maxTermMonths} meses`,
          });
          hasValidationError = true;
        }

        if (hasValidationError) return;
      }

      run(() => createLoanApplication(data));
    },
    [run, selectedLoanType, setError],
  );

  const handleReset = useCallback(() => {
    reset();
  }, [reset]);

  return (
    <>
      <Card className="mx-auto w-full max-w-2xl border border-slate-200/80 bg-white rounded-2xl shadow-sm">
        <Card.Header className="border-b border-slate-100 p-6">
          <div className="flex items-center gap-3">
            <div className="flex size-10 items-center justify-center rounded-xl bg-blue-50 text-blue-600 border border-blue-100">
              <FilePlus className="size-5" />
            </div>
            <div>
              <Card.Title className="text-lg font-bold text-slate-900">
                Nueva Solicitud de Préstamo
              </Card.Title>
              <Card.Description className="text-xs text-slate-500">
                Configure las condiciones del préstamo y calcule las cuotas estimadas
              </Card.Description>
            </div>
          </div>
        </Card.Header>

        <Card.Content className="p-6">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Sección 1: Selección del Cliente */}
            {userType === "staff" && (
              <div className="space-y-4">
                <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
                  <User className="size-4 text-blue-600" />
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                    1. Selección de Cliente
                  </h3>
                </div>

                <TypedController<LoanApplicationFormData>
                  name="customerId"
                  control={control}
                  render={({ value, onChange, error }) => (
                    <div className="space-y-1">
                      <Autocomplete
                        fullWidth
                        placeholder="Buscar o seleccionar cliente..."
                        selectionMode="single"
                        value={value as string}
                        onChange={(key) => key && onChange(String(key))}
                      >
                        <Label className="text-xs font-semibold text-slate-700">
                          Cliente Solicitante <span className="text-rose-500">*</span>
                        </Label>
                        <Autocomplete.Trigger>
                          <Autocomplete.Value />
                          <Autocomplete.ClearButton />
                          <Autocomplete.Indicator />
                        </Autocomplete.Trigger>
                        <Autocomplete.Popover aria-label="Seleccionar cliente">
                          <Autocomplete.Filter filter={contains}>
                            <SearchField name="customerSearch" variant="secondary">
                              <SearchField.Group>
                                <SearchField.SearchIcon />
                                <SearchField.Input
                                  aria-label="Buscar cliente"
                                  placeholder="Buscar por nombre o email..."
                                />
                                <SearchField.ClearButton />
                              </SearchField.Group>
                            </SearchField>
                            <ListBox
                              renderEmptyState={() => (
                                <EmptyState>No se encontraron clientes</EmptyState>
                              )}
                            >
                              {customers.map((c) => (
                                <ListBox.Item
                                  key={c.id}
                                  id={c.id}
                                  textValue={`${c.firstName} ${c.lastName} ${c.email}`}
                                >
                                  {c.firstName} {c.lastName} — {c.email}
                                  <ListBox.ItemIndicator />
                                </ListBox.Item>
                              ))}
                            </ListBox>
                          </Autocomplete.Filter>
                        </Autocomplete.Popover>
                      </Autocomplete>
                      {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                    </div>
                  )}
                />
              </div>
            )}

            {/* Sección 2: Tipo de Préstamo */}
            <div className="space-y-4 pt-2">
              <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
                <Wallet className="size-4 text-blue-600" />
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                  2. Tipo de Préstamo
                </h3>
              </div>

              <TypedController<LoanApplicationFormData>
                name="loanTypeId"
                control={control}
                render={({ value, onChange, error }) => (
                  <div className="space-y-1">
                    <Select
                      fullWidth
                      placeholder="Seleccione el tipo de préstamo..."
                      selectedKey={value as string}
                      onSelectionChange={(key) => key && onChange(String(key))}
                    >
                      <Label className="text-xs font-semibold text-slate-700">
                        Línea de Crédito <span className="text-rose-500">*</span>
                      </Label>
                      <Select.Trigger>
                        <Select.Value />
                        <Select.Indicator />
                      </Select.Trigger>
                      <Select.Popover>
                        <ListBox>
                          {loanTypes.map((lt) => (
                            <ListBox.Item key={lt.id} id={lt.id} textValue={lt.name}>
                              {lt.name} — {lt.interestRate}% anual
                              <ListBox.ItemIndicator />
                            </ListBox.Item>
                          ))}
                        </ListBox>
                      </Select.Popover>
                    </Select>
                    {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                  </div>
                )}
              />

              {selectedLoanType && (
                <LoanTypeTermsNotice loanType={selectedLoanType} />
              )}
            </div>

            {/* Sección 3: Monto y Plazo */}
            <div className="space-y-4 pt-2">
              <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
                <Calculator className="size-4 text-blue-600" />
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                  3. Monto y Plazo Solicitado
                </h3>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <TypedController<LoanApplicationFormData>
                  name="requestedAmount"
                  control={control}
                  render={({ value, onChange, error }) => (
                    <TextField isInvalid={!!error}>
                      <div className="flex items-center justify-between">
                        <Label className="text-xs font-semibold text-slate-700">
                          Monto Solicitado (COP) <span className="text-rose-500">*</span>
                        </Label>
                        {Number(value) > 0 && (
                          <span className="text-xs font-bold text-blue-700">
                            {formatCurrency(Number(value))}
                          </span>
                        )}
                      </div>
                      <Input
                        id="requestedAmount"
                        type="number"
                        min={selectedLoanType?.minAmount}
                        max={selectedLoanType?.maxAmount}
                        placeholder="0"
                        value={String(value ?? 0)}
                        onChange={(e) => onChange(Number(e.target.value))}
                      />
                      {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                    </TextField>
                  )}
                />

                <TypedController<LoanApplicationFormData>
                  name="termInMonths"
                  control={control}
                  render={({ value, onChange, error }) => (
                    <TextField isInvalid={!!error}>
                      <Label className="text-xs font-semibold text-slate-700">
                        Plazo (Meses) <span className="text-rose-500">*</span>
                      </Label>
                      <Input
                        id="termInMonths"
                        type="number"
                        min={selectedLoanType?.minTermMonths}
                        max={selectedLoanType?.maxTermMonths}
                        placeholder="Ej. 12"
                        value={String(value ?? 0)}
                        onChange={(e) => onChange(Number(e.target.value))}
                      />
                      {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                    </TextField>
                  )}
                />
              </div>
            </div>

            {/* Live Amortization Calculator Summary Box */}
            {estimatedSummary && (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50/60 p-4 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold uppercase tracking-wider text-emerald-800 flex items-center gap-1.5">
                    <CheckCircle2 className="size-4 text-emerald-600" />
                    Simulación de Cuota Estimada
                  </span>
                  <span className="text-xs font-semibold text-emerald-700 bg-white px-2 py-0.5 rounded-full border border-emerald-200">
                    {estimatedSummary.annualRate}% Tasa Anual
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-2 text-center pt-1">
                  <div className="bg-white p-2.5 rounded-xl border border-emerald-100">
                    <span className="block text-[11px] font-medium text-slate-500">
                      Cuota Mensual
                    </span>
                    <span className="text-sm font-bold text-slate-900">
                      {formatCurrency(estimatedSummary.monthlyPayment)}
                    </span>
                  </div>
                  <div className="bg-white p-2.5 rounded-xl border border-emerald-100">
                    <span className="block text-[11px] font-medium text-slate-500">
                      Total Intereses
                    </span>
                    <span className="text-sm font-bold text-slate-900">
                      {formatCurrency(estimatedSummary.totalInterest)}
                    </span>
                  </div>
                  <div className="bg-white p-2.5 rounded-xl border border-emerald-100">
                    <span className="block text-[11px] font-medium text-slate-500">
                      Total a Pagar
                    </span>
                    <span className="text-sm font-bold text-emerald-700">
                      {formatCurrency(estimatedSummary.totalPayment)}
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* Botones de acción */}
            <div className="flex flex-col-reverse gap-3 pt-4 sm:flex-row sm:justify-end border-t border-slate-100">
              <Button
                type="button"
                variant="tertiary"
                className="h-10 text-slate-600 hover:bg-slate-100"
                onPress={handleReset}
              >
                <RotateCcw className="size-4" />
                Limpiar Formulario
              </Button>
              <Button
                type="submit"
                isPending={isLoading}
                className="h-10 bg-blue-600 font-semibold text-white shadow-xs hover:bg-blue-500"
              >
                <FilePlus className="size-4" />
                Crear Solicitud
              </Button>
            </div>
          </form>
        </Card.Content>
      </Card>

      <LoanApplicationResultModal
        isOpen={showResultModal}
        onOpenChange={setShowResultModal}
        result={applicationResult}
        onClose={() => {
          setShowResultModal(false);
          reset();
        }}
      />
    </>
  );
}
