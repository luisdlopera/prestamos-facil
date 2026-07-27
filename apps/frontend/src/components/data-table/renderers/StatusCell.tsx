import { Chip } from "@heroui/react";
import type { CellRendererProps } from "../types";
import type { BadgeOptions } from "../types";

const defaultStatusColors: Record<string, string> = {
  PENDING_REVIEW: "warning",
  APPROVED: "success",
  REJECTED: "danger",
  MANUAL_REVIEW: "warning",
  PENDING: "warning",
  ACTIVE: "success",
  PAID: "success",
  DEFAULTED: "danger",
  WRITTEN_OFF: "default",
};

const defaultStatusLabels: Record<string, string> = {
  PENDING_REVIEW: "Pendiente de Revisión",
  APPROVED: "Aprobada",
  REJECTED: "Rechazada",
  MANUAL_REVIEW: "Revisión Manual",
  PENDING: "Pendiente",
  ACTIVE: "Activo",
  PAID: "Pagado",
  DEFAULTED: "Vencido",
  WRITTEN_OFF: "Cancelado",
};

const StatusCell: React.FC<CellRendererProps> = ({ value, column }) => {
  const opts = (column.options ?? {}) as BadgeOptions;
  const { colorMap, labelMap, variantMap } = opts;

  if (value === null || value === undefined || value === "") {
    return <span className="text-default-400 select-none">−</span>;
  }

  const strValue = String(value);
  const label = labelMap?.[strValue] ?? defaultStatusLabels[strValue] ?? strValue;
  const color = (colorMap?.[strValue] ??
    defaultStatusColors[strValue] ??
    "default") as React.ComponentProps<typeof Chip>["color"];
  const variant = (variantMap?.[strValue] ?? "soft") as React.ComponentProps<
    typeof Chip
  >["variant"];

  return (
    <Chip color={color} size="sm" variant={variant}>
      {label}
    </Chip>
  );
};

export default StatusCell;
