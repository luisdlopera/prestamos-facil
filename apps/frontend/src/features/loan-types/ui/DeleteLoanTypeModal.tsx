"use client";

import { Modal, Button } from "@heroui/react";
import { AlertTriangle } from "lucide-react";
import type { LoanTypeDto } from "@/lib/api/loan-types-api";

interface DeleteLoanTypeModalProps {
  item: LoanTypeDto | null;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
  isLoading: boolean;
}

export function DeleteLoanTypeModal({
  item,
  onOpenChange,
  onConfirm,
  isLoading,
}: DeleteLoanTypeModalProps) {
  return (
    <Modal>
      <Modal.Backdrop isOpen={Boolean(item)} onOpenChange={(open) => !open && onOpenChange(false)}>
        <Modal.Container>
          <Modal.Dialog className="sm:max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 p-6 space-y-4">
            <Modal.CloseTrigger />
            <div className="flex items-center gap-3 text-amber-600">
              <AlertTriangle className="size-8" />
              <h3 className="text-lg font-bold text-slate-900">¿Eliminar Tipo de Crédito?</h3>
            </div>
            <p className="text-sm text-slate-600">
              ¿Estás seguro de que deseas eliminar permanentemente <strong>{item?.name}</strong>?
            </p>
            <p className="text-xs text-amber-700 bg-amber-50 p-3 rounded-lg border border-amber-200">
              Nota: Si este tipo de crédito tiene solicitudes o préstamos asociados, el sistema
              impedirá su eliminación física y se requerirá desactivarlo.
            </p>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" onPress={() => onOpenChange(false)} isDisabled={isLoading}>
                Cancelar
              </Button>
              <Button
                variant="danger"
                className="bg-rose-600 hover:bg-rose-700 text-white font-semibold"
                onPress={onConfirm}
                isDisabled={isLoading}
              >
                Eliminar
              </Button>
            </div>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
