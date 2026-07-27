export const DOCUMENT_TYPES = {
  CC: "CC",
  CE: "CE",
  NIT: "NIT",
  TI: "TI",
  PP: "PP",
  PEP: "PEP",
} as const;

export type DocumentType = (typeof DOCUMENT_TYPES)[keyof typeof DOCUMENT_TYPES];

export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  CC: "Cédula de Ciudadanía",
  CE: "Cédula de Extranjería",
  NIT: "NIT",
  TI: "Tarjeta de Identidad",
  PP: "Pasaporte",
  PEP: "Permiso Especial de Permanencia",
};
