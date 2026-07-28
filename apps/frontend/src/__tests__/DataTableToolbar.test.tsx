import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import { DataTableToolbar } from "../components/ui/DataTable/DataTableToolbar";

describe("DataTableToolbar", () => {
  it("renders status options using HeroUI Select", () => {
    const statusOptions = [
      { label: "Pendiente", value: "PENDING" },
      { label: "Aprobado", value: "APPROVED" },
    ];
    const onStatusChangeMock = vi.fn();

    render(
      <DataTableToolbar
        statusOptions={statusOptions}
        selectedStatus="PENDING"
        onStatusChange={onStatusChangeMock}
      />,
    );

    const selectButton = screen.getByRole("button", { name: /Filtrar por estado/i });
    expect(selectButton).toBeInTheDocument();
  });

  it("handles search inputs and status options correctly", () => {
    const statusOptions = [
      { label: "Pendiente", value: "PENDING" },
      { label: "Aprobado", value: "APPROVED" },
    ];
    const onSearchValueChangeMock = vi.fn();
    const onSearchMock = vi.fn();
    const onStatusChangeMock = vi.fn();

    render(
      <DataTableToolbar
        searchValue="test"
        onSearchValueChange={onSearchValueChangeMock}
        onSearch={onSearchMock}
        statusOptions={statusOptions}
        selectedStatus="PENDING"
        onStatusChange={onStatusChangeMock}
      />,
    );

    const searchInput = screen.getByPlaceholderText(/Buscar/i);
    expect(searchInput).toBeInTheDocument();
    expect(searchInput).toHaveValue("test");
  });

  it("calls onStatusChange when an option is selected", () => {
    const statusOptions = [
      { label: "Pendiente", value: "PENDING" },
      { label: "Aprobado", value: "APPROVED" },
    ];
    const onStatusChangeMock = vi.fn();

    render(
      <DataTableToolbar
        statusOptions={statusOptions}
        selectedStatus="PENDING"
        onStatusChange={onStatusChangeMock}
      />,
    );

    const selectButton = screen.getByRole("button", { name: /Filtrar por estado/i });
    expect(selectButton).toBeInTheDocument();
  });
});
