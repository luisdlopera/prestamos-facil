import { describe, it, expect, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen, fireEvent } from "@testing-library/react";
import {
  SCHEMA_TABLES,
  SCHEMA_RELATIONSHIPS,
} from "../features/relational-model/domain/schemaData";
import { EntityCard } from "../features/relational-model/components/EntityCard";
import { DiagramToolbar } from "../features/relational-model/components/DiagramToolbar";

describe("Relational Model Domain Schema Data", () => {
  it("should contain the current database tables without redundant lifecycle/token tables", () => {
    expect(SCHEMA_TABLES).toHaveLength(12);
    const tableNames = SCHEMA_TABLES.map((t) => t.name);
    expect(tableNames).toContain("users");
    expect(tableNames).toContain("customers");
    expect(tableNames).toContain("user_roles");
    expect(tableNames).toContain("auth_tokens");
    expect(tableNames).toContain("loan_applications");
    expect(tableNames).toContain("loan_application_status_history");
    expect(tableNames).toContain("loans");
    expect(tableNames).toContain("payment_installments");
    expect(tableNames).toContain("roles");
    expect(tableNames).toContain("document_types");
    expect(tableNames).not.toContain("staff");
    expect(tableNames).not.toContain("staff_roles");
    expect(tableNames).not.toContain("loan_statuses");
    expect(tableNames).not.toContain("password_reset_tokens");
    expect(tableNames).not.toContain("refresh_tokens");
  });

  it("should define relationships with explicit cardinalities", () => {
    expect(SCHEMA_RELATIONSHIPS.length).toBeGreaterThan(10);
    const loanInstallmentsRel = SCHEMA_RELATIONSHIPS.find(
      (r) => r.fromTable === "loans" && r.toTable === "payment_installments",
    );
    expect(loanInstallmentsRel).toBeDefined();
    expect(loanInstallmentsRel?.toNotation).toBe("1..N");
  });
});

describe("EntityCard Component", () => {
  it("should render table name, category, and PK/FK columns", () => {
    const sampleTable = SCHEMA_TABLES.find((t) => t.name === "customers")!;
    render(
      <EntityCard
        table={sampleTable}
        position={{ x: 100, y: 100 }}
        isSelected={false}
        isHighlighted={false}
        onPointerDown={vi.fn()}
        onPointerMove={vi.fn()}
        onPointerUp={vi.fn()}
        onSelect={vi.fn()}
      />,
    );

    expect(screen.getByText("customers")).toBeInTheDocument();
    expect(screen.getAllByText(/Clientes/i).length).toBeGreaterThan(0);
    expect(screen.getByText("first_name")).toBeInTheDocument();
  });
});

describe("DiagramToolbar Component", () => {
  it("should render search input and handle search changes", () => {
    const handleSearchChange = vi.fn();
    render(
      <DiagramToolbar
        searchTerm=""
        onSearchChange={handleSearchChange}
        selectedCategory="all"
        onSelectCategory={vi.fn()}
        zoom={1}
        onZoomIn={vi.fn()}
        onZoomOut={vi.fn()}
        onResetZoom={vi.fn()}
        onResetLayout={vi.fn()}
        onOpenLegend={vi.fn()}
        totalTables={12}
        totalRelationships={15}
      />,
    );

    expect(screen.getByText(/Modelo Relacional de Base de Datos/i)).toBeInTheDocument();
    expect(screen.getByText(/12 tablas/i)).toBeInTheDocument();

    const searchInput = screen.getByPlaceholderText(/Buscar tabla o columna/i);
    fireEvent.change(searchInput, { target: { value: "users" } });
    expect(handleSearchChange).toHaveBeenCalledWith("users");
  });
});
