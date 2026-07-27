import { memo } from "react";
import { X, Key, Link as LinkIcon, Info } from "lucide-react";

interface CardinalityLegendProps {
  isOpen: boolean;
  onClose: () => void;
}

export const CardinalityLegend = memo(function CardinalityLegend({
  isOpen,
  onClose,
}: CardinalityLegendProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="relative w-full max-w-lg rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl p-6 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-600 dark:text-blue-400">
              <Info size={20} />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900 dark:text-slate-100">
                Guía de Notación y Cardinalidad
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Notación Pata de Gallina (Crow&apos;s Foot)
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Content */}
        <div className="py-4 space-y-5 text-xs">
          {/* Key Identifiers */}
          <div>
            <h3 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">
              Llaves e Identificadores
            </h3>
            <div className="grid grid-cols-2 gap-3">
              <div className="flex items-center gap-2 p-2.5 rounded-xl bg-amber-500/10 border border-amber-500/20">
                <span className="p-1 rounded bg-amber-500/20 text-amber-600 dark:text-amber-400">
                  <Key size={14} />
                </span>
                <div>
                  <div className="font-semibold text-amber-900 dark:text-amber-200">
                    Primary Key (PK)
                  </div>
                  <div className="text-[10px] text-amber-700 dark:text-amber-300">
                    Llave primaria de la tabla
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2 p-2.5 rounded-xl bg-blue-500/10 border border-blue-500/20">
                <span className="p-1 rounded bg-blue-500/20 text-blue-600 dark:text-blue-400">
                  <LinkIcon size={14} />
                </span>
                <div>
                  <div className="font-semibold text-blue-900 dark:text-blue-200">
                    Foreign Key (FK)
                  </div>
                  <div className="text-[10px] text-blue-700 dark:text-blue-300">
                    Llave foránea referente
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Crow's Foot Cardinalities */}
          <div>
            <h3 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">
              Símbolos de Cardinalidad (Crow&apos;s Foot)
            </h3>
            <div className="space-y-2">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <span className="px-2 py-1 rounded bg-slate-200 dark:bg-slate-700 font-mono font-bold text-slate-900 dark:text-slate-100">
                    1
                  </span>
                  <div>
                    <div className="font-medium text-slate-900 dark:text-slate-100">
                      Exactly One (1)
                    </div>
                    <div className="text-[10px] text-slate-500 dark:text-slate-400">
                      Un único registro obligatorio en la entidad padre
                    </div>
                  </div>
                </div>
                <span className="font-mono text-[11px] text-slate-500">||</span>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <span className="px-2 py-1 rounded bg-blue-500/20 font-mono font-bold text-blue-600 dark:text-blue-400">
                    0..N
                  </span>
                  <div>
                    <div className="font-medium text-slate-900 dark:text-slate-100">
                      Zero or Many (0..N)
                    </div>
                    <div className="text-[10px] text-slate-500 dark:text-slate-400">
                      Cero, uno o múltiples registros asociados (Pata de Gallina abierta)
                    </div>
                  </div>
                </div>
                <span className="font-mono text-[11px] text-slate-500">&gt;o</span>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <span className="px-2 py-1 rounded bg-emerald-500/20 font-mono font-bold text-emerald-600 dark:text-emerald-400">
                    1..N
                  </span>
                  <div>
                    <div className="font-medium text-slate-900 dark:text-slate-100">
                      One or Many (1..N)
                    </div>
                    <div className="text-[10px] text-slate-500 dark:text-slate-400">
                      Al menos un registro obligatorio o más asociados (Pata de Gallina cerrada)
                    </div>
                  </div>
                </div>
                <span className="font-mono text-[11px] text-slate-500">|&lt;</span>
              </div>

              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700">
                <div className="flex items-center gap-3">
                  <span className="px-2 py-1 rounded bg-purple-500/20 font-mono font-bold text-purple-600 dark:text-purple-400">
                    0..1
                  </span>
                  <div>
                    <div className="font-medium text-slate-900 dark:text-slate-100">
                      Zero or One (0..1)
                    </div>
                    <div className="text-[10px] text-slate-500 dark:text-slate-400">
                      Relación opcional de máximo un registro (1 a 1 opcional)
                    </div>
                  </div>
                </div>
                <span className="font-mono text-[11px] text-slate-500">o|</span>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 text-xs font-semibold rounded-xl bg-slate-900 text-white dark:bg-slate-100 dark:text-slate-900 hover:opacity-90 transition-opacity"
          >
            Entendido
          </button>
        </div>
      </div>
    </div>
  );
});
