import type { ReactNode } from "react";

export type ColumnType =
  | "text"
  | "number"
  | "currency"
  | "date"
  | "datetime"
  | "boolean"
  | "badge"
  | "status"
  | "email"
  | "phone"
  | "actions"
  | "custom";

export interface BaseColumnOptions {
  emptyText?: string;
  truncate?: boolean;
  className?: string;
}

export interface CurrencyOptions extends BaseColumnOptions {
  currency?: string;
  locale?: string;
  showSymbol?: boolean;
}

export interface DatetimeOptions extends BaseColumnOptions {
  format?: string;
  locale?: string;
}

export interface BadgeOptions extends BaseColumnOptions {
  variantMap?: Record<string, string>;
  labelMap?: Record<string, string>;
  colorMap?: Record<string, string>;
}

export interface CustomOptions extends BaseColumnOptions {
  render: (value: unknown, row: unknown) => ReactNode;
}

export interface DataTableColumn<T = unknown> {
  key: keyof T | (string & {});
  label?: string;
  header?: string;
  type?: ColumnType;
  options?: BaseColumnOptions & Record<string, unknown>;
  sortable?: boolean;
  searchable?: boolean;
  align?: "left" | "center" | "right";
  width?: number | string;
  minWidth?: number;
  render?: (row: T) => ReactNode;
}

export interface CellRendererProps<T = unknown> {
  column: DataTableColumn<T>;
  value: unknown;
  row: T;
}

export type CellRendererComponent<T = unknown> = React.FC<CellRendererProps<T>>;

export type ExtractOptions<T extends ColumnType> = T extends "currency"
  ? CurrencyOptions
  : T extends "datetime" | "date"
    ? DatetimeOptions
    : T extends "badge" | "status"
      ? BadgeOptions
      : T extends "custom"
        ? CustomOptions
        : BaseColumnOptions;

export function getColumnOptions<T extends ColumnType>(column: DataTableColumn): ExtractOptions<T> {
  return (column.options ?? {}) as unknown as ExtractOptions<T>;
}
