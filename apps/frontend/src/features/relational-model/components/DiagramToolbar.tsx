import { memo } from "react";
import { ZoomIn, ZoomOut, RotateCcw, Search, HelpCircle, Database, ArrowLeft } from "lucide-react";
import type { ModuleCategory } from "../domain/schema.types";

interface DiagramToolbarProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  selectedCategory: ModuleCategory | "all";
  onSelectCategory: (cat: ModuleCategory | "all") => void;
  zoom: number;
  onZoomIn: () => void;
  onZoomOut: () => void;
  onResetZoom: () => void;
  onResetLayout: () => void;
  onOpenLegend: () => void;
  totalTables: number;
  totalRelationships: number;
}

const CATEGORY_TABS: { id: ModuleCategory | "all"; label: string }[] = [
  { id: "all", label: "Todas" },
  { id: "clientes", label: "Clientes" },
  { id: "prestamos", label: "Préstamos" },
  { id: "staff", label: "Staff & Auth" },
  { id: "referencia", label: "Referencia" },
];

export const DiagramToolbar = memo(function DiagramToolbar({
  searchTerm,
  onSearchChange,
  selectedCategory,
  onSelectCategory,
  zoom,
  onZoomIn,
  onZoomOut,
  onResetZoom,
  onResetLayout,
  onOpenLegend,
  totalTables,
  totalRelationships,
}: DiagramToolbarProps) {
  return (
    <header className="sticky top-0 z-40 flex flex-wrap items-center justify-between gap-4 px-4 py-3 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 shadow-sm">
      {/* Title & Brand */}
      <div className="flex items-center gap-3">
        <a
          href="/dashboard"
          className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
          title="Volver al Dashboard"
        >
          <ArrowLeft size={18} />
        </a>
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-xl bg-blue-600 text-white shadow-md shadow-blue-500/20">
            <Database size={18} />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
              Modelo Relacional de Base de Datos
              <span className="text-[11px] font-medium px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20">
                Pata de Gallina (Crow&apos;s Foot)
              </span>
            </h1>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              {totalTables} tablas · {totalRelationships} relaciones con cardinalidad explicita
            </p>
          </div>
        </div>
      </div>

      {/* Middle Controls: Search & Category Filters */}
      <div className="flex items-center gap-3 flex-1 max-w-2xl">
        {/* Search */}
        <div className="relative flex-1 min-w-[200px]">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Buscar tabla o columna..."
            className="w-full pl-9 pr-3 py-1.5 text-xs rounded-xl bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
          />
        </div>

        {/* Categories */}
        <div className="hidden sm:flex items-center gap-1 p-1 rounded-xl bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700">
          {CATEGORY_TABS.map((cat) => (
            <button
              key={cat.id}
              onClick={() => onSelectCategory(cat.id)}
              className={`px-2.5 py-1 text-[11px] font-medium rounded-lg transition-all ${
                selectedCategory === cat.id
                  ? "bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-sm"
                  : "text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100"
              }`}
            >
              {cat.label}
            </button>
          ))}
        </div>
      </div>

      {/* Zoom & Action Controls */}
      <div className="flex items-center gap-2">
        {/* Zoom Group */}
        <div className="flex items-center rounded-xl bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 p-0.5">
          <button
            onClick={onZoomOut}
            className="p-1.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-white dark:hover:bg-slate-900 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
            title="Reducir zoom (-)"
          >
            <ZoomOut size={16} />
          </button>
          <button
            onClick={onResetZoom}
            className="px-2 py-1 text-xs font-mono font-medium text-slate-700 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-900 rounded-lg"
            title="Restablecer zoom (100%)"
          >
            {Math.round(zoom * 100)}%
          </button>
          <button
            onClick={onZoomIn}
            className="p-1.5 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-white dark:hover:bg-slate-900 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
            title="Aumentar zoom (+)"
          >
            <ZoomIn size={16} />
          </button>
        </div>

        {/* Reset Layout */}
        <button
          onClick={onResetLayout}
          className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
          title="Reorganizar posición de tablas"
        >
          <RotateCcw size={16} />
        </button>

        {/* Legend Button */}
        <button
          onClick={onOpenLegend}
          className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-xl bg-blue-600 text-white hover:bg-blue-700 shadow-sm transition-colors"
        >
          <HelpCircle size={15} />
          <span>Leyenda</span>
        </button>
      </div>
    </header>
  );
});
