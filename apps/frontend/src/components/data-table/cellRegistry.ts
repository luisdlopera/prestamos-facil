import type { ColumnType, CellRendererComponent } from "./types";

import TextCell from "./renderers/TextCell";
import NumberCell from "./renderers/NumberCell";
import CurrencyCell from "./renderers/CurrencyCell";
import DatetimeCell from "./renderers/DatetimeCell";
import BooleanCell from "./renderers/BooleanCell";
import BadgeCell from "./renderers/BadgeCell";
import StatusCell from "./renderers/StatusCell";
import EmailCell from "./renderers/EmailCell";
import PhoneCell from "./renderers/PhoneCell";
import ActionsCell from "./renderers/ActionsCell";
import CustomCell from "./renderers/CustomCell";

const registry: Record<ColumnType, CellRendererComponent> = {
  text: TextCell,
  number: NumberCell,
  currency: CurrencyCell,
  date: DatetimeCell,
  datetime: DatetimeCell,
  boolean: BooleanCell,
  badge: BadgeCell,
  status: StatusCell,
  email: EmailCell,
  phone: PhoneCell,
  actions: ActionsCell,
  custom: CustomCell,
};

export function getCellRenderer(type: ColumnType): CellRendererComponent {
  return registry[type] ?? TextCell;
}

export function registerCellRenderer(type: ColumnType, renderer: CellRendererComponent): void {
  registry[type] = renderer;
}
