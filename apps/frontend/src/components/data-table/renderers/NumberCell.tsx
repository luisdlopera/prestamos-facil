import type { CellRendererProps } from "../types";

const NumberCell: React.FC<CellRendererProps> = ({ value, column }) => {
  const emptyText = (column.options?.emptyText as string) ?? "−";

  if (value === null || value === undefined || value === "" || Number.isNaN(Number(value))) {
    return <span className="text-default-400 select-none">{emptyText}</span>;
  }

  const num = Number(value);
  const locale = (column.options as Record<string, string>)?.locale ?? "es-CO";

  return <span className="text-foreground tabular-nums">{num.toLocaleString(locale)}</span>;
};

export default NumberCell;
