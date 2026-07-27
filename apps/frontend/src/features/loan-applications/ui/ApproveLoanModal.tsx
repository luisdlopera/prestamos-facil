"use client";

import { Button, Modal } from "@heroui/react";
import { Check } from "lucide-react";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";

interface ApproveLoanModalProps {
  isOpen: boolean;
  onOpenChange: (isOpen: boolean) => void;
  application: LoanApplicationDto | null;
  onConfirm: (application: LoanApplicationDto) => void;
  isProcessing?: boolean;
}

export function ApproveLoanModal({
  isOpen,
  onOpenChange,
  application,
  onConfirm,
  isProcessing,
}: ApproveLoanModalProps) {
  if (!application) return null;

  return (
    <Modal>
      <Modal.Backdrop isOpen={isOpen} onOpenChange={onOpenChange}>
        <Modal.Container>
          <Modal.Dialog className="sm:max-w-[400px]">
            <Modal.CloseTrigger />
            <Modal.Header>
              <Modal.Heading>Aprobar Solicitud</Modal.Heading>
            </Modal.Header>
            <Modal.Body>
              <p className="text-sm text-muted">
                ¿Está seguro de aprobar la solicitud de <strong>{application.customerName}</strong>{" "}
                por{" "}
                <strong>
                  {new Intl.NumberFormat("es-CO", {
                    style: "currency",
                    currency: "COP",
                    minimumFractionDigits: 0,
                  }).format(application.requestedAmount)}
                </strong>{" "}
                a {application.termInMonths} meses?
              </p>
              <p className="text-sm text-muted">
                Se generará el plan de pagos y se notificará al solicitante.
              </p>
            </Modal.Body>
            <Modal.Footer>
              <Button slot="close" variant="tertiary" isDisabled={isProcessing}>
                Cancelar
              </Button>
              <Button onPress={() => onConfirm(application)} isPending={isProcessing}>
                <Check className="size-4" />
                Aprobar
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
