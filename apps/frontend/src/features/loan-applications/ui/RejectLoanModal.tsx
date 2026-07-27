"use client";

import { Button, Input, Modal } from "@heroui/react";
import { XCircle } from "lucide-react";
import { useState } from "react";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";

interface RejectLoanModalProps {
  isOpen: boolean;
  onOpenChange: (isOpen: boolean) => void;
  application: LoanApplicationDto | null;
  onConfirm: (application: LoanApplicationDto, reason: string) => void;
  isProcessing?: boolean;
}

export function RejectLoanModal({
  isOpen,
  onOpenChange,
  application,
  onConfirm,
  isProcessing,
}: RejectLoanModalProps) {
  const [reason, setReason] = useState("");

  if (!application) return null;

  return (
    <Modal>
      <Modal.Backdrop isOpen={isOpen} onOpenChange={onOpenChange}>
        <Modal.Container>
          <Modal.Dialog className="sm:max-w-[400px]">
            <Modal.CloseTrigger />
            <Modal.Header>
              <Modal.Heading>Rechazar Solicitud</Modal.Heading>
            </Modal.Header>
            <Modal.Body className="space-y-4">
              <p className="text-sm text-muted">
                ¿Está seguro de rechazar la solicitud de <strong>{application.customerName}</strong>
                ?
              </p>
              <Input
                placeholder="Ingrese el motivo del rechazo..."
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
              />
            </Modal.Body>
            <Modal.Footer>
              <Button slot="close" variant="tertiary" isDisabled={isProcessing}>
                Cancelar
              </Button>
              <Button
                variant="danger"
                onPress={() => onConfirm(application, reason)}
                isPending={isProcessing}
                isDisabled={!reason.trim()}
              >
                <XCircle className="size-4" />
                Rechazar
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
