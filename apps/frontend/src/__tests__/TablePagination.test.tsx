import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { TablePagination } from "../components/ui/DataTable/TablePagination";

describe("TablePagination", () => {
  it("renders page info and page size selector when total items exceeds page size", () => {
    const onChangeMock = vi.fn();
    const onPageSizeChangeMock = vi.fn();

    render(
      <TablePagination
        page={1}
        totalPages={3}
        totalItems={25}
        pageSize={10}
        onChange={onChangeMock}
        onPageSizeChange={onPageSizeChangeMock}
      />,
    );

    expect(screen.getByText(/Mostrando/i)).toBeInTheDocument();
    const pageSizeSelect = screen.getByRole("button", { name: /Registros por página/i });
    expect(pageSizeSelect).toBeInTheDocument();
  });
});
