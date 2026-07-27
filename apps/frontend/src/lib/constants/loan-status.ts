export const LOAN_APPLICATION_STATUS = {
  PENDIENTE_REVISION: "PENDIENTE_REVISION",
  REVISION_MANUAL: "REVISION_MANUAL",
  APROBADA: "APROBADA",
  RECHAZADA: "RECHAZADA",
} as const;

export type LoanApplicationStatus =
  (typeof LOAN_APPLICATION_STATUS)[keyof typeof LOAN_APPLICATION_STATUS];

export const LOAN_STATUS = {
  ACTIVO: "ACTIVO",
  PAGADO: "PAGADO",
  VENCIDO: "VENCIDO",
  CANCELADO: "CANCELADO",
} as const;

export type LoanStatus = (typeof LOAN_STATUS)[keyof typeof LOAN_STATUS];

export const LOAN_APPLICATION_STATUS_LABELS: Record<LoanApplicationStatus, string> = {
  PENDIENTE_REVISION: "Pendiente de Revisión",
  REVISION_MANUAL: "Revisión Manual",
  APROBADA: "Aprobada",
  RECHAZADA: "Rechazada",
};

export const LOAN_STATUS_LABELS: Record<LoanStatus, string> = {
  ACTIVO: "Activo",
  PAGADO: "Pagado",
  VENCIDO: "Vencido",
  CANCELADO: "Cancelado",
};
