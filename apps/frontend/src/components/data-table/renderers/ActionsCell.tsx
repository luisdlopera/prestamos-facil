import { useCallback, useMemo } from "react";
import { Button, Dropdown, Label } from "@heroui/react";
import { MoreVertical } from "lucide-react";
import type { CellRendererProps } from "../types";

export interface TableAction<T = unknown> {
  key: string;
  label: string;
  icon?: React.ReactNode;
  variant?: "primary" | "secondary" | "tertiary" | "outline" | "ghost" | "danger" | "default";
  onAction?: (row: T) => void;
  href?: string;
  disabled?: boolean;
}

export function TableActionsDropdown<T>({ actions, row }: { actions: TableAction<T>[]; row: T }) {
  const handleAction = useCallback(
    (key: string) => {
      const targetAction = actions.find((act) => act.key === key);
      if (!targetAction || targetAction.disabled) return;

      if (targetAction.onAction) {
        targetAction.onAction(row);
      } else if (targetAction.href) {
        window.location.href = targetAction.href;
      }
    },
    [actions, row],
  );

  if (!actions || actions.length === 0) {
    return null;
  }

  return (
    <Dropdown>
      <Button isIconOnly size="sm" variant="tertiary" aria-label="Acciones">
        <MoreVertical className="size-4" />
      </Button>
      <Dropdown.Popover className="min-w-[160px]">
        <Dropdown.Menu onAction={(key) => handleAction(String(key))}>
          {actions.map((action) => (
            <Dropdown.Item
              key={action.key}
              id={action.key}
              textValue={action.label}
              variant={action.variant === "danger" ? "danger" : undefined}
              href={action.href}
            >
              {action.icon && (
                <span className="mr-2 inline-flex items-center size-4">{action.icon}</span>
              )}
              <Label>{action.label}</Label>
            </Dropdown.Item>
          ))}
        </Dropdown.Menu>
      </Dropdown.Popover>
    </Dropdown>
  );
}

const ActionsCell: React.FC<CellRendererProps> = ({ column, row }) => {
  const actions: TableAction[] = useMemo(() => {
    const rawActions = column.options?.actions;
    if (typeof rawActions === "function") {
      return rawActions(row);
    }
    if (Array.isArray(rawActions)) {
      return rawActions;
    }
    return [];
  }, [column.options?.actions, row]);

  return <TableActionsDropdown actions={actions} row={row} />;
};

export default ActionsCell;
