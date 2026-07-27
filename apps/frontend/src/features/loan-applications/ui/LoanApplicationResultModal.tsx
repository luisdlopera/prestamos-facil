"use client";

import { Button, Modal } from "@heroui/react";
import { CheckCircle2, AlertCircle, XCircle } from "lucide-react";
import type { LoanApplicationDto } from "../infrastructure/loan-applications-api";

interface LoanApplicationResultModalProps {
  isOpen: boolean;
  onOpenChange: (isOpen: boolean) => void;
  result: LoanApplicationDto | null;
  onClose: () => void;
}

type StatusConfig = {
  icon: typeof CheckCircle2;
  color: "emerald" | "amber" | "rose";
  title: string;
  subtitle: string;
  description: string;
  detail: string;
};

const STATUS_CONFIG: Record<string, StatusConfig> = {
  APPROVED: {
    icon: CheckCircle2,
    color: "emerald",
    title: "¡Solicitud Aprobada!",
    subtitle: "Tu solicitud ha sido aprobada automáticamente",
    description: "Felicidades, tu solicitud ha sido aprobada.",
    detail:
      "El desembolso se realizará en las próximas horas. Recibirás un correo con el plan de pagos detallado.",
  },
  MANUAL_REVIEW: {
    icon: AlertCircle,
    color: "amber",
    title: "Solicitud en Revisión",
    subtitle: "Tu solicitud ha sido enviada a revisión manual",
    description: "Tu solicitud está siendo evaluada por nuestro equipo.",
    detail:
      "Te notificaremos por correo cuando tengamos una decisión. Este proceso puede tomar hasta 24 horas hábiles.",
  },
  REJECTED: {
    icon: XCircle,
    color: "rose",
    title: "Solicitud No Aprobada",
    subtitle: "Lo sentimos, tu solicitud no pudo ser aprobada",
    description: "La solicitud no cumple con los criterios de aprobación.",
    detail: "Puedes intentar con un monto menor o contactar a nuestro equipo para más información.",
  },
};

const STATUS_COLORS = {
  emerald: {
    header: "border-b border-emerald-100 bg-emerald-50/80",
    icon: "bg-emerald-100 text-emerald-600",
    title: "text-emerald-900",
    subtitle: "text-emerald-700",
    body: "border-emerald-200 bg-emerald-50",
    text: "text-emerald-900",
    detailText: "text-emerald-700",
  },
  amber: {
    header: "border-b border-amber-100 bg-amber-50/80",
    icon: "bg-amber-100 text-amber-600",
    title: "text-amber-900",
    subtitle: "text-amber-700",
    body: "border-amber-200 bg-amber-50",
    text: "text-amber-900",
    detailText: "text-amber-700",
  },
  rose: {
    header: "border-b border-rose-100 bg-rose-50/80",
    icon: "bg-rose-100 text-rose-600",
    title: "text-rose-900",
    subtitle: "text-rose-700",
    body: "border-rose-200 bg-rose-50",
    text: "text-rose-900",
    detailText: "text-rose-700",
  },
};

export function LoanApplicationResultModal({
  isOpen,
  onOpenChange,
  result,
  onClose,
}: LoanApplicationResultModalProps) {
  if (!result) return null;

  const config = STATUS_CONFIG[result.status];
  if (!config) return null;

  const Icon = config.icon;
  const colors = STATUS_COLORS[config.color];

  return (
    <Modal>
      <Modal.Backdrop isOpen={isOpen} onOpenChange={onOpenChange}>
        <Modal.Container>
          <Modal.Dialog className="sm:max-w-md bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden">
            <Modal.CloseTrigger />
            <Modal.Header className={`flex items-center gap-3 ${colors.header} p-6`}>
              <div
                className={`flex size-12 items-center justify-center rounded-full ${colors.icon}`}
              >
                <Icon className="size-6" />
              </div>
              <div>
                <Modal.Heading className={`text-lg font-bold ${colors.title}`}>
                  {config.title}
                </Modal.Heading>
                <p className={`text-xs ${colors.subtitle}`}>{config.subtitle}</p>
              </div>
            </Modal.Header>
            <Modal.Body className="p-6 space-y-4">
              <div className={`rounded-xl border ${colors.body} p-4 text-sm ${colors.text}`}>
                <p className="font-semibold">{config.description}</p>
                <p className={`mt-2 ${colors.detailText}`}>{config.detail}</p>
              </div>
              {result.decisionReason && (
                <div className="text-xs text-slate-500 bg-slate-50 rounded-lg p-3">
                  <span className="font-semibold">Motivo:</span> {result.decisionReason}
                </div>
              )}
            </Modal.Body>
            <Modal.Footer className="flex items-center justify-end gap-3 p-4 border-t border-slate-100 bg-slate-50/50">
              <Button
                variant="primary"
                className="bg-blue-600 hover:bg-blue-700 font-semibold"
                onPress={onClose}
              >
                Cerrar
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
