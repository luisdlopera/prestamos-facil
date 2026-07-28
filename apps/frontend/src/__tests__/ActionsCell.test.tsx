import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import ActionsCell, { TableActionsDropdown } from "../components/data-table/renderers/ActionsCell";
import type { TableAction } from "../components/data-table/renderers/ActionsCell";
import type { DataTableColumn } from "../components/data-table/types";

describe("TableActionsDropdown", () => {
  it("renders trigger button when actions are provided", () => {
    const actions: TableAction[] = [{ key: "edit", label: "Editar", onAction: vi.fn() }];
    render(<TableActionsDropdown actions={actions} row={{ id: 1 }} />);

    const button = screen.getByRole("button", { name: "Acciones" });
    expect(button).toBeInTheDocument();
  });

  it("returns null when actions array is empty", () => {
    const { container } = render(<TableActionsDropdown actions={[]} row={{ id: 1 }} />);
    expect(container.firstChild).toBeNull();
  });

  it("renders ActionsCell via column options correctly", () => {
    const onActionMock = vi.fn();
    const column: DataTableColumn = {
      key: "actions",
      options: {
        actions: [{ key: "view", label: "Ver Detalles", onAction: onActionMock }],
      },
    };

    render(<ActionsCell column={column} value={null} row={{ id: 10 }} />);
    const button = screen.getByRole("button", { name: "Acciones" });
    expect(button).toBeInTheDocument();
  });
});
