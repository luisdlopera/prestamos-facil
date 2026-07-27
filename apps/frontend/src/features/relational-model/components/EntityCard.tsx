import React, { memo } from "react";
import { Key, Link as LinkIcon, Database, Move } from "lucide-react";
import type { TableEntity, ModuleCategory } from "../domain/schema.types";

interface EntityCardProps {
  table: TableEntity;
  position: { x: number; y: number };
  isSelected: boolean;
  isHighlighted: boolean;
  onPointerDown: (e: React.PointerEvent, tableId: string) => void;
  onPointerMove: (e: React.PointerEvent, tableId: string) => void;
  onPointerUp: (e: React.PointerEvent, tableId: string) => void;
  onSelect: (tableId: string) => void;
}

const CATEGORY_STYLES: Record<
  ModuleCategory,
  { bg: string; text: string; border: string; badge: string }
> = {
  referencia: {
    bg: "bg-purple-500/10",
    text: "text-purple-600 dark:text-purple-400",
    border: "border-purple-500/30",
    badge: "bg-purple-500/20 text-purple-700 dark:text-purple-300",
  },
  clientes: {
    bg: "bg-emerald-500/10",
    text: "text-emerald-600 dark:text-emerald-400",
    border: "border-emerald-500/30",
    badge: "bg-emerald-500/20 text-emerald-700 dark:text-emerald-300",
  },
  prestamos: {
    bg: "bg-blue-500/10",
    text: "text-blue-600 dark:text-blue-400",
    border: "border-blue-500/30",
    badge: "bg-blue-500/20 text-blue-700 dark:text-blue-300",
  },
  staff: {
    bg: "bg-amber-500/10",
    text: "text-amber-600 dark:text-amber-400",
    border: "border-amber-500/30",
    badge: "bg-amber-500/20 text-amber-700 dark:text-amber-300",
  },
};

export const EntityCard = memo(function EntityCard({
  table,
  position,
  isSelected,
  isHighlighted,
  onPointerDown,
  onPointerMove,
  onPointerUp,
  onSelect,
}: EntityCardProps) {
  const styles = CATEGORY_STYLES[table.category];

  return (
    <div
      id={`table-card-${table.id}`}
      data-diagram-card="true"
      role="button"
      tabIndex={0}
      style={{
        transform: `translate(${position.x}px, ${position.y}px)`,
        touchAction: "none",
      }}
      onPointerDown={(e) => {
        e.stopPropagation();
        onPointerDown(e, table.id);
      }}
      onPointerMove={(e) => {
        onPointerMove(e, table.id);
      }}
      onPointerUp={(e) => {
        onPointerUp(e, table.id);
      }}
      onPointerCancel={(e) => {
        onPointerUp(e, table.id);
      }}
      onClick={(e) => {
        e.stopPropagation();
        onSelect(table.id);
      }}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.stopPropagation();
          onSelect(table.id);
        }
      }}
      className={`absolute w-72 rounded-xl bg-white dark:bg-slate-900 border transition-shadow duration-150 shadow-md z-10 select-none cursor-grab active:cursor-grabbing ${
        isSelected
          ? "border-blue-500 ring-4 ring-blue-500/20 shadow-2xl z-30"
          : isHighlighted
            ? "border-blue-400 dark:border-blue-500 ring-2 ring-blue-400/20 z-20"
            : "border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700"
      }`}
    >
      {/* Header */}
      <div
        className={`flex items-center justify-between px-3 py-2.5 rounded-t-xl border-b ${styles.border} ${styles.bg}`}
      >
        <div className="flex items-center gap-2 min-w-0">
          <Database className={`size-4 shrink-0 ${styles.text}`} />
          <div className="min-w-0">
            <h3 className="font-semibold text-xs text-slate-900 dark:text-slate-100 truncate">
              {table.name}
            </h3>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">
              {table.displayName}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <span
            className={`text-[9px] font-semibold uppercase px-1.5 py-0.5 rounded-md ${styles.badge}`}
          >
            {table.category}
          </span>
          <Move className="size-3.5 text-slate-400 opacity-60" />
        </div>
      </div>

      {/* Columns List */}
      <ul className="divide-y divide-slate-100 dark:divide-slate-800/60 max-h-80 overflow-y-auto text-[11px]">
        {table.columns.map((col) => (
          <li
            key={col.name}
            className={`flex items-center justify-between px-3 py-1.5 hover:bg-slate-50 dark:hover:bg-slate-800/40 ${
              col.isPk ? "bg-amber-500/5 font-medium" : col.isFk ? "bg-blue-500/5" : ""
            }`}
          >
            <div className="flex items-center gap-1.5 min-w-0">
              {col.isPk ? (
                <span
                  title="Primary Key (Llave Primaria)"
                  className="inline-flex items-center justify-center size-4 rounded bg-amber-500/20 text-amber-600 dark:text-amber-400 shrink-0"
                >
                  <Key size={10} className="stroke-[2.5]" />
                </span>
              ) : col.isFk ? (
                <span
                  title={`Foreign Key -> ${col.fkTargetTable}.${col.fkTargetColumn}`}
                  className="inline-flex items-center justify-center size-4 rounded bg-blue-500/20 text-blue-600 dark:text-blue-400 shrink-0"
                >
                  <LinkIcon size={10} className="stroke-[2.5]" />
                </span>
              ) : (
                <span className="size-4 shrink-0" />
              )}
              <span
                className={`truncate ${
                  col.isPk
                    ? "font-semibold text-amber-900 dark:text-amber-200"
                    : col.isFk
                      ? "font-medium text-blue-900 dark:text-blue-200"
                      : "text-slate-700 dark:text-slate-300"
                }`}
              >
                {col.name}
              </span>
            </div>

            <div className="flex items-center gap-1 shrink-0 ml-2">
              <span className="text-[10px] text-slate-400 font-mono">{col.type}</span>
              {col.nullable && (
                <span className="text-[9px] text-slate-400 bg-slate-100 dark:bg-slate-800 px-1 rounded">
                  null
                </span>
              )}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
});
