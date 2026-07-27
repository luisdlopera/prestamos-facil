export type LoanApplicationStatus =
  "PENDIENTE_REVISION" | "REVISION_MANUAL" | "APROBADA" | "RECHAZADA";

export interface LoanApplicationSummary {
  id: string;
  cliente: string;
  correo: string;
  monto: number;
  plazo: number;
  tipo: string;
  estado: LoanApplicationStatus;
  fecha: string;
}
