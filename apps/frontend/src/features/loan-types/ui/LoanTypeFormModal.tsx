"use client";

import { useState, useCallback } from "react";
import {
  Modal,
  Input,
  TextArea,
  Select,
  ListBox,
  Switch,
  Button,
  Description,
} from "@heroui/react";
import type { Key } from "@heroui/react";
import { Layers } from "lucide-react";
import type { LoanTypeDto, CreateLoanTypePayload } from "@/lib/api/loan-types-api";

export interface FormState {
  id?: string;
  name: string;
  description: string;
  interestRate: string;
  rateType: "EA";
  minAmount: string;
  maxAmount: string;
  minTermMonths: string;
  maxTermMonths: string;
  displayOrder: number;
  active: boolean;
  automaticValidationEnabled: boolean;
}

const initialFormState: FormState = {
  name: "",
  description: "",
  interestRate: "18.50",
  rateType: "EA",
  minAmount: "1000000",
  maxAmount: "50000000",
  minTermMonths: "6",
  maxTermMonths: "60",
  displayOrder: 0,
  active: true,
  automaticValidationEnabled: false,
};

interface LoanTypeFormModalProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  editingItem: LoanTypeDto | null;
  defaultDisplayOrder: number;
  onSubmit: (payload: CreateLoanTypePayload, id?: string) => void;
  isLoading: boolean;
}

function buildFormFromDto(item: LoanTypeDto): FormState {
  return {
    id: item.id,
    name: item.name ?? "",
    description: item.description ?? "",
    interestRate: String(item.interestRate ?? 0),
    rateType: item.rateType || "EA",
    minAmount: String(item.minAmount ?? 0),
    maxAmount: String(item.maxAmount ?? 0),
    minTermMonths: String(item.minTermMonths ?? 1),
    maxTermMonths: String(item.maxTermMonths ?? 60),
    displayOrder: item.displayOrder ?? 0,
    active: item.active ?? true,
    automaticValidationEnabled: item.automaticValidationEnabled ?? false,
  };
}

const normalizeDecimal = (str: string) => str.replace(",", ".");

function validateForm(formData: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!formData.name.trim()) errors.name = "El nombre es obligatorio.";
  else if (formData.name.trim().length > 100)
    errors.name = "El nombre no puede exceder 100 caracteres.";
  if (formData.description.trim().length > 500)
    errors.description = "La descripción no puede exceder 500 caracteres.";

  const rate = parseFloat(normalizeDecimal(formData.interestRate));
  if (!Number.isFinite(rate) || rate < 0 || rate > 100) {
    errors.interestRate = "Ingresa una tasa válida entre 0 y 100.";
  }

  const minAmt = parseFloat(normalizeDecimal(formData.minAmount));
  if (!Number.isFinite(minAmt) || minAmt <= 0) errors.minAmount = "Monto mínimo inválido.";

  const maxAmt = parseFloat(normalizeDecimal(formData.maxAmount));
  if (!Number.isFinite(maxAmt) || maxAmt < minAmt) {
    errors.maxAmount = "El monto máximo debe ser mayor o igual al mínimo.";
  }

  const minTerm = parseInt(formData.minTermMonths, 10);
  if (!Number.isFinite(minTerm) || minTerm < 1)
    errors.minTermMonths = "Plazo mínimo inválido (mín. 1).";

  const maxTerm = parseInt(formData.maxTermMonths, 10);
  if (!Number.isFinite(maxTerm) || maxTerm < minTerm) {
    errors.maxTermMonths = "El plazo máximo debe ser mayor o igual al mínimo.";
  }

  return errors;
}

function buildPayload(formData: FormState): CreateLoanTypePayload {
  return {
    name: formData.name.trim(),
    description: formData.description.trim(),
    interestRate: parseFloat(normalizeDecimal(formData.interestRate)),
    rateType: formData.rateType,
    minAmount: parseFloat(normalizeDecimal(formData.minAmount)),
    maxAmount: parseFloat(normalizeDecimal(formData.maxAmount)),
    minTermMonths: parseInt(formData.minTermMonths, 10),
    maxTermMonths: parseInt(formData.maxTermMonths, 10),
    displayOrder: Number(formData.displayOrder) || 0,
    active: formData.active,
    automaticValidationEnabled: formData.automaticValidationEnabled,
  };
}

export function LoanTypeFormModal({
  isOpen,
  onOpenChange,
  editingItem,
  defaultDisplayOrder,
  onSubmit,
  isLoading,
}: LoanTypeFormModalProps) {
  const [formData, setFormData] = useState<FormState>(() =>
    editingItem
      ? buildFormFromDto(editingItem)
      : { ...initialFormState, displayOrder: defaultDisplayOrder },
  );
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  const handleRateTypeChange = useCallback((key: Key | null) => {
    if (key) {
      setFormData((prev) => ({ ...prev, rateType: "EA" }));
    }
  }, []);

  const handleSubmit = useCallback(() => {
    const errors = validateForm(formData);
    setFormErrors(errors);
    if (Object.keys(errors).length > 0) return;
    onSubmit(buildPayload(formData), editingItem?.id);
  }, [formData, editingItem, onSubmit]);

  return (
    <Modal>
      <Modal.Backdrop isOpen={isOpen} onOpenChange={onOpenChange}>
        <Modal.Container>
          <Modal.Dialog className="sm:max-w-xl bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
            <Modal.CloseTrigger />
            <Modal.Header className="flex items-center gap-3 border-b border-slate-100 bg-slate-50/80 p-6">
              <div className="flex size-10 items-center justify-center rounded-xl bg-blue-100 text-blue-600">
                <Layers className="size-5" />
              </div>
              <div>
                <Modal.Heading className="text-lg font-bold text-slate-900">
                  {editingItem ? "Editar Tipo de Crédito" : "Crear Tipo de Crédito"}
                </Modal.Heading>
                <p className="text-xs text-slate-500">
                  Configura los detalles técnicos de la línea de crédito.
                </p>
              </div>
            </Modal.Header>

            <Modal.Body className="p-6 space-y-4 max-h-[75vh] overflow-y-auto">
              <div className="space-y-1">
                <span className="block text-xs font-semibold uppercase text-slate-600">
                  Nombre del Tipo de Crédito *
                </span>
                <Input
                  placeholder="ej. Libre Inversión, Vehículo, Vivienda"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full"
                />
                {formErrors.name && <p className="text-xs text-rose-500 mt-1">{formErrors.name}</p>}
              </div>

              <div className="space-y-1">
                <span className="block text-xs font-semibold uppercase text-slate-600">
                  Descripción
                </span>
                <TextArea
                  placeholder="Breve explicación de las condiciones y destinación del crédito"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full min-h-[70px]"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Tasa de Interés (%) *
                  </span>
                  <Input
                    type="number"
                    placeholder="18.50"
                    value={formData.interestRate}
                    onChange={(e) => setFormData({ ...formData, interestRate: e.target.value })}
                    className="w-full"
                  />
                  {formErrors.interestRate && (
                    <p className="text-xs text-rose-500 mt-1">{formErrors.interestRate}</p>
                  )}
                </div>

                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Tipo de Tasa *
                  </span>
                  <Select
                    aria-label="Tipo de Tasa"
                    placeholder="Seleccionar tipo de tasa"
                    selectedKey={formData.rateType}
                    onSelectionChange={handleRateTypeChange}
                    className="w-full"
                  >
                    <Select.Trigger>
                      <Select.Value />
                      <Select.Indicator />
                    </Select.Trigger>
                    <Select.Popover>
                      <ListBox aria-label="Tipo de Tasa">
                        <ListBox.Item id="EA" textValue="EA - Efectiva Anual">
                          EA - Efectiva Anual
                          <ListBox.ItemIndicator />
                        </ListBox.Item>
                      </ListBox>
                    </Select.Popover>
                  </Select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Monto Mínimo ($) *
                  </span>
                  <Input
                    type="number"
                    placeholder="1000000"
                    value={formData.minAmount}
                    onChange={(e) => setFormData({ ...formData, minAmount: e.target.value })}
                    className="w-full"
                  />
                  {formErrors.minAmount && (
                    <p className="text-xs text-rose-500 mt-1">{formErrors.minAmount}</p>
                  )}
                </div>

                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Monto Máximo ($) *
                  </span>
                  <Input
                    type="number"
                    placeholder="50000000"
                    value={formData.maxAmount}
                    onChange={(e) => setFormData({ ...formData, maxAmount: e.target.value })}
                    className="w-full"
                  />
                  {formErrors.maxAmount && (
                    <p className="text-xs text-rose-500 mt-1">{formErrors.maxAmount}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Plazo Mínimo (Meses) *
                  </span>
                  <Input
                    type="number"
                    placeholder="6"
                    value={formData.minTermMonths}
                    onChange={(e) => setFormData({ ...formData, minTermMonths: e.target.value })}
                    className="w-full"
                  />
                  {formErrors.minTermMonths && (
                    <p className="text-xs text-rose-500 mt-1">{formErrors.minTermMonths}</p>
                  )}
                </div>

                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Plazo Máximo (Meses) *
                  </span>
                  <Input
                    type="number"
                    placeholder="60"
                    value={formData.maxTermMonths}
                    onChange={(e) => setFormData({ ...formData, maxTermMonths: e.target.value })}
                    className="w-full"
                  />
                  {formErrors.maxTermMonths && (
                    <p className="text-xs text-rose-500 mt-1">{formErrors.maxTermMonths}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 items-start pt-2">
                <div className="space-y-1">
                  <span className="block text-xs font-semibold uppercase text-slate-600">
                    Orden de Visualización
                  </span>
                  <Input
                    type="number"
                    placeholder="0"
                    value={String(formData.displayOrder)}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        displayOrder: parseInt(e.target.value, 10) || 0,
                      })
                    }
                    className="w-full"
                  />
                </div>

                <div className="flex flex-col gap-4 pt-1">
                  <Switch
                    isSelected={formData.active}
                    onChange={(isSelected) => setFormData({ ...formData, active: isSelected })}
                  >
                    <Switch.Content>
                      <Switch.Control>
                        <Switch.Thumb />
                      </Switch.Control>
                      <span className="text-sm font-semibold text-slate-700">
                        Activar en Formulario
                      </span>
                    </Switch.Content>
                  </Switch>

                  <Switch
                    isSelected={formData.automaticValidationEnabled}
                    onChange={(isSelected) =>
                      setFormData({ ...formData, automaticValidationEnabled: isSelected })
                    }
                  >
                    <Switch.Content>
                      <Switch.Control>
                        <Switch.Thumb />
                      </Switch.Control>
                      <span className="text-sm font-semibold text-slate-700">
                        Validación Automática
                      </span>
                    </Switch.Content>
                    <Description className="block text-xs text-slate-400 mt-0.5">
                      Evaluar solicitudes automáticamente al crearlas
                    </Description>
                  </Switch>
                </div>
              </div>
            </Modal.Body>

            <Modal.Footer className="flex items-center justify-end gap-3 p-4 border-t border-slate-100 bg-slate-50/50">
              <Button variant="outline" onPress={() => onOpenChange(false)} isDisabled={isLoading}>
                Cancelar
              </Button>
              <Button
                variant="primary"
                className="bg-blue-600 hover:bg-blue-700 font-semibold"
                onPress={handleSubmit}
                isDisabled={isLoading}
              >
                {editingItem ? "Guardar Cambios" : "Crear Tipo de Crédito"}
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
