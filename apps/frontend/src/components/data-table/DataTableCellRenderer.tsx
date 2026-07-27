import type { CellRendererProps } from "./types";
import { getCellRenderer } from "./cellRegistry";

export const DataTableCellRenderer = <T extends Record<string, unknown>>({
  column,
  value,
  row,
}: CellRendererProps<T>) => {
  const effectiveType = column.type || "text";
  const Renderer = getCellRenderer(effectiveType);

  return (
    <Renderer
      column={column as unknown as CellRendererProps["column"]}
      value={value}
      row={row as unknown as Record<string, unknown>}
    />
  );
};
