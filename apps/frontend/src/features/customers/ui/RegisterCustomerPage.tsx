"use client";

import { useCallback } from "react";
import { Button, Card, Input, Label, ListBox, Select, TextField } from "@heroui/react";
import { RotateCcw, UserPlus, User, Wallet, IdCard } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { customerSchema } from "@/lib/schemas";
import type { CustomerFormData } from "@/lib/schemas";
import { createCustomer } from "../infrastructure/customers-api";
import type { CustomerDto } from "../infrastructure/customers-api";
import { useAsyncAction } from "@/lib/errors";
import { TypedController } from "@/components/ui/TypedController";
import { DOCUMENT_TYPE_LABELS } from "@/lib/constants";
import { formatCurrency } from "@/lib/formatters/currency";

export function RegisterCustomerPage() {
  const { control, handleSubmit, reset, watch } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      documentType: "CC",
      documentNumber: "",
      baseSalary: 0,
    },
  });

  const baseSalaryValue = watch("baseSalary");

  const { run, isLoading } = useAsyncAction<CustomerDto>({
    successMsg: "Cliente registrado exitosamente",
    onSuccess: () => reset(),
  });

  const onSubmit = useCallback(
    (data: CustomerFormData) => {
      run(() => createCustomer(data));
    },
    [run],
  );

  const handleReset = useCallback(() => {
    reset();
  }, [reset]);

  return (
    <Card className="mx-auto w-full max-w-2xl border border-slate-200/80 bg-white rounded-2xl shadow-sm">
      <Card.Header className="border-b border-slate-100 p-6">
        <div className="flex items-center gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-blue-50 text-blue-600 border border-blue-100">
            <UserPlus className="size-5" />
          </div>
          <div>
            <Card.Title className="text-lg font-bold text-slate-900">
              Registrar Nuevo Cliente
            </Card.Title>
            <Card.Description className="text-xs text-slate-500">
              Ingrese la información básica, contacto e identificación para dar de alta al cliente
            </Card.Description>
          </div>
        </div>
      </Card.Header>

      <Card.Content className="p-6">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Sección 1: Datos Personales */}
          <div className="space-y-4">
            <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
              <User className="size-4 text-blue-600" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                1. Información Personal
              </h3>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <TypedController<CustomerFormData>
                name="firstName"
                control={control}
                render={({ value, onChange, error }) => (
                  <TextField isInvalid={!!error}>
                    <Label className="text-xs font-semibold text-slate-700">
                      Nombres <span className="text-rose-500">*</span>
                    </Label>
                    <Input
                      id="firstName"
                      placeholder="Ej. Juan Carlos"
                      value={value as string}
                      onChange={(e) => onChange(e.target.value)}
                    />
                    {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                  </TextField>
                )}
              />

              <TypedController<CustomerFormData>
                name="lastName"
                control={control}
                render={({ value, onChange, error }) => (
                  <TextField isInvalid={!!error}>
                    <Label className="text-xs font-semibold text-slate-700">
                      Apellidos <span className="text-rose-500">*</span>
                    </Label>
                    <Input
                      id="lastName"
                      placeholder="Ej. Pérez Gómez"
                      value={value as string}
                      onChange={(e) => onChange(e.target.value)}
                    />
                    {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                  </TextField>
                )}
              />
            </div>

            <TypedController<CustomerFormData>
              name="email"
              control={control}
              render={({ value, onChange, error }) => (
                <TextField isInvalid={!!error}>
                  <Label className="text-xs font-semibold text-slate-700">
                    Correo Electrónico <span className="text-rose-500">*</span>
                  </Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="ejemplo@correo.com"
                    value={value as string}
                    onChange={(e) => onChange(e.target.value)}
                  />
                  {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                </TextField>
              )}
            />
          </div>

          {/* Sección 2: Identificación y Contacto */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
              <IdCard className="size-4 text-blue-600" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                2. Identificación y Contacto
              </h3>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <TypedController<CustomerFormData>
                name="documentType"
                control={control}
                render={({ value, onChange, error }) => (
                  <div className="space-y-1">
                    <Select
                      fullWidth
                      placeholder="Seleccionar tipo..."
                      selectedKey={value as string}
                      onSelectionChange={(key) => key && onChange(String(key))}
                    >
                      <Label className="text-xs font-semibold text-slate-700">
                        Tipo de Documento
                      </Label>
                      <Select.Trigger>
                        <Select.Value />
                        <Select.Indicator />
                      </Select.Trigger>
                      <Select.Popover>
                        <ListBox>
                          {Object.entries(DOCUMENT_TYPE_LABELS).map(([key, label]) => (
                            <ListBox.Item key={key} id={key} textValue={label}>
                              {label}
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

              <TypedController<CustomerFormData>
                name="documentNumber"
                control={control}
                render={({ value, onChange, error }) => (
                  <TextField isInvalid={!!error}>
                    <Label className="text-xs font-semibold text-slate-700">
                      Número de Documento <span className="text-rose-500">*</span>
                    </Label>
                    <Input
                      id="documentNumber"
                      placeholder="Sin puntos ni guiones"
                      value={value as string}
                      onChange={(e) => onChange(e.target.value)}
                    />
                    {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                  </TextField>
                )}
              />
            </div>
          </div>

          {/* Sección 3: Información Financiera */}
          <div className="space-y-4 pt-2">
            <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
              <Wallet className="size-4 text-blue-600" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700">
                3. Información Financiera
              </h3>
            </div>

            <TypedController<CustomerFormData>
              name="baseSalary"
              control={control}
              render={({ value, onChange, error }) => (
                <TextField isInvalid={!!error}>
                  <div className="flex items-center justify-between">
                    <Label className="text-xs font-semibold text-slate-700">
                      Salario Base Mensual (COP) <span className="text-rose-500">*</span>
                    </Label>
                    {baseSalaryValue > 0 && (
                      <span className="text-xs font-bold text-blue-700">
                        {formatCurrency(baseSalaryValue)}
                      </span>
                    )}
                  </div>
                  <Input
                    id="baseSalary"
                    type="number"
                    placeholder="0"
                    value={String(value ?? 0)}
                    onChange={(e) => onChange(Number(e.target.value))}
                  />
                  {error && <p className="text-xs text-rose-500 font-medium">{error}</p>}
                </TextField>
              )}
            />
          </div>

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
              <UserPlus className="size-4" />
              Registrar Cliente
            </Button>
          </div>
        </form>
      </Card.Content>
    </Card>
  );
}
