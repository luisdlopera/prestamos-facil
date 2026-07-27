export type ModuleCategory = "clientes" | "prestamos" | "staff" | "referencia";

export interface TableColumn {
  name: string;
  type: string;
  isPk?: boolean;
  isFk?: boolean;
  fkTargetTable?: string;
  fkTargetColumn?: string;
  nullable?: boolean;
}

export interface Relationship {
  id: string;
  fromTable: string;
  fromColumn: string;
  toTable: string;
  toColumn: string;
  cardinality: "1:1" | "1:N" | "0..1:0..N" | "1:1..N" | "0..N:1";
  fromNotation: "1" | "0..1";
  toNotation: "0..N" | "1..N" | "0..1" | "1";
  description: string;
}

export interface TableEntity {
  id: string;
  name: string;
  displayName: string;
  category: ModuleCategory;
  description: string;
  initialX: number;
  initialY: number;
  columns: TableColumn[];
}

export interface Point {
  x: number;
  y: number;
}
