import { useMemo } from "react";
import type { CellRendererProps } from "../types";
import type { DatetimeOptions } from "../types";

const DatetimeCell: React.FC<CellRendererProps> = ({ value, column }) => {
  const { format: formatStr, locale = "es-CO" } = (column.options as DatetimeOptions) ?? {};

  const formatter = useMemo(() => {
    if (formatStr) {
      return new Intl.DateTimeFormat(locale, {
        dateStyle: formatStr as Intl.DateTimeFormatOptions["dateStyle"],
      });
    }

    switch (column.type) {
      case "date":
        return new Intl.DateTimeFormat(locale, {
          year: "numeric",
          month: "short",
          day: "2-digit",
        });
      default:
        return new Intl.DateTimeFormat(locale, {
          year: "numeric",
          month: "short",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
        });
    }
  }, [locale, formatStr, column.type]);

  if (value === null || value === undefined || value === "") {
    return <span className="text-default-400 select-none">−</span>;
  }

  const valueString = String(value);
  const date = column.type === "date" && /^\d{4}-\d{2}-\d{2}$/.test(valueString)
    ? new Date(`${valueString}T12:00:00`)
    : new Date(valueString);
  if (Number.isNaN(date.getTime())) {
    return <span className="text-danger-500">Invalid Date</span>;
  }

  return <span className="text-foreground whitespace-nowrap">{formatter.format(date)}</span>;
};

export default DatetimeCell;
