import { Chip } from "@heroui/react";
import type { CellRendererProps } from "../types";
import type { BadgeOptions } from "../types";

const BadgeCell: React.FC<CellRendererProps> = ({ value, column }) => {
  const opts = (column.options ?? {}) as BadgeOptions;
  const { colorMap, labelMap, variantMap } = opts;

  if (value === null || value === undefined || value === "") {
    return <span className="text-default-400 select-none">−</span>;
  }

  const strValue = String(value);

  const defaultColors: Record<string, string> = {
    ACTIVE: "success",
    INACTIVE: "danger",
    PENDING: "warning",
    COMPLETED: "success",
    CANCELLED: "danger",
    ARCHIVED: "default",
  };

  const defaultLabels: Record<string, string> = {
    ACTIVE: "Activo",
    INACTIVE: "Inactivo",
    PENDING: "Pendiente",
    COMPLETED: "Completado",
    CANCELLED: "Cancelado",
    ARCHIVED: "Archivado",
  };

  const label = labelMap?.[strValue] ?? defaultLabels[strValue] ?? strValue;
  const color = (colorMap?.[strValue] ??
    defaultColors[strValue] ??
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

export default BadgeCell;
