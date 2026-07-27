/* eslint-disable jsx-a11y/no-noninteractive-tabindex */
import React, { useState, useCallback, useMemo, useRef, useEffect } from "react";
import { SCHEMA_TABLES, SCHEMA_RELATIONSHIPS } from "../domain/schemaData";
import type { ModuleCategory } from "../domain/schema.types";
import { EntityCard } from "./EntityCard";
import { RelationshipLines } from "./RelationshipLines";
import { DiagramToolbar } from "./DiagramToolbar";
import { CardinalityLegend } from "./CardinalityLegend";
import { Link2, X } from "lucide-react";

// Canvas virtual dimensions — duplicated from original 5000×3500
const CANVAS_WIDTH = 10000;
const CANVAS_HEIGHT = 7000;

export function RelationalDiagramContainer() {
  const [positions, setPositions] = useState<Record<string, { x: number; y: number }>>(() => {
    const initial: Record<string, { x: number; y: number }> = {};
    SCHEMA_TABLES.forEach((t) => {
      initial[t.id] = { x: t.initialX, y: t.initialY };
    });
    return initial;
  });

  const [zoom, setZoom] = useState(0.55);
  const [panOffset, setPanOffset] = useState({ x: 20, y: 20 });
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<ModuleCategory | "all">("all");
  const [selectedTableId, setSelectedTableId] = useState<string | null>(null);
  const [selectedRelationshipId, setSelectedRelationshipId] = useState<string | null>(null);
  const [hoveredRelationshipId, setHoveredRelationshipId] = useState<string | null>(null);
  const [isLegendOpen, setIsLegendOpen] = useState(false);

  // Use refs for drag/pan state to avoid React re-render overhead during pointer events
  const draggingTableIdRef = useRef<string | null>(null);
  const dragStartRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const isPanningRef = useRef(false);
  const panStartRef = useRef<{ x: number; y: number }>({ x: 0, y: 0 });

  const containerRef = useRef<HTMLDivElement>(null);
  const zoomRef = useRef(zoom);
  useEffect(() => {
    zoomRef.current = zoom;
  }, [zoom]);

  // Filter tables by search and category
  const filteredTables = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    return SCHEMA_TABLES.filter((t) => {
      const matchCategory = selectedCategory === "all" || t.category === selectedCategory;
      if (!matchCategory) return false;
      if (!term) return true;

      const nameMatch =
        t.name.toLowerCase().includes(term) || t.displayName.toLowerCase().includes(term);
      const colMatch = t.columns.some((c) => c.name.toLowerCase().includes(term));
      return nameMatch || colMatch;
    });
  }, [searchTerm, selectedCategory]);

  const filteredTableIds = useMemo(
    () => new Set(filteredTables.map((t) => t.id)),
    [filteredTables],
  );

  // Filter relationships connecting visible tables
  const visibleRelationships = useMemo(() => {
    return SCHEMA_RELATIONSHIPS.filter(
      (r) => filteredTableIds.has(r.fromTable) && filteredTableIds.has(r.toTable),
    );
  }, [filteredTableIds]);

  // Selected relationship details
  const activeRelationship = useMemo(() => {
    if (!selectedRelationshipId) return null;
    return SCHEMA_RELATIONSHIPS.find((r) => r.id === selectedRelationshipId) || null;
  }, [selectedRelationshipId]);

  // Compute connected tables for selected table OR selected relationship
  const connectedTableIds = useMemo(() => {
    const connected = new Set<string>();

    if (selectedRelationshipId) {
      const rel = SCHEMA_RELATIONSHIPS.find((r) => r.id === selectedRelationshipId);
      if (rel) {
        connected.add(rel.fromTable);
        connected.add(rel.toTable);
      }
    }

    if (selectedTableId) {
      connected.add(selectedTableId);
      SCHEMA_RELATIONSHIPS.forEach((r) => {
        if (r.fromTable === selectedTableId) connected.add(r.toTable);
        if (r.toTable === selectedTableId) connected.add(r.fromTable);
      });
    }

    return connected;
  }, [selectedTableId, selectedRelationshipId]);

  // Zoom handlers
  const handleZoomIn = useCallback(() => setZoom((z) => Math.min(2.5, z + 0.15)), []);
  const handleZoomOut = useCallback(() => setZoom((z) => Math.max(0.15, z - 0.15)), []);
  const handleResetZoom = useCallback(() => {
    setZoom(0.55);
    setPanOffset({ x: 20, y: 20 });
  }, []);

  const handleResetLayout = useCallback(() => {
    const initial: Record<string, { x: number; y: number }> = {};
    SCHEMA_TABLES.forEach((t) => {
      initial[t.id] = { x: t.initialX, y: t.initialY };
    });
    setPositions(initial);
    setZoom(0.55);
    setPanOffset({ x: 20, y: 20 });
    setSelectedTableId(null);
    setSelectedRelationshipId(null);
  }, []);

  // ── Pointer-based drag for tables ──────────────────────────────────────────
  // Using PointerEvents + setPointerCapture ensures the move events always
  // reach the element even when the pointer leaves it rapidly.
  const handleTablePointerDown = useCallback((e: React.PointerEvent, tableId: string) => {
    e.stopPropagation();
    (e.currentTarget as HTMLElement).setPointerCapture(e.pointerId);
    draggingTableIdRef.current = tableId;
    dragStartRef.current = { x: e.clientX, y: e.clientY };
    setSelectedTableId(tableId);
    setSelectedRelationshipId(null);
  }, []);

  const handleTablePointerMove = useCallback((e: React.PointerEvent, tableId: string) => {
    if (draggingTableIdRef.current !== tableId) return;
    const currentZoom = zoomRef.current;
    const dx = (e.clientX - dragStartRef.current.x) / currentZoom;
    const dy = (e.clientY - dragStartRef.current.y) / currentZoom;

    setPositions((prev) => ({
      ...prev,
      [tableId]: {
        x: Math.max(-500, Math.min(CANVAS_WIDTH - 100, (prev[tableId]?.x || 0) + dx)),
        y: Math.max(-500, Math.min(CANVAS_HEIGHT - 100, (prev[tableId]?.y || 0) + dy)),
      },
    }));

    dragStartRef.current = { x: e.clientX, y: e.clientY };
  }, []);

  const handleTablePointerUp = useCallback(() => {
    draggingTableIdRef.current = null;
  }, []);

  // ── Canvas pan via pointer events on the container ──────────────────────────
  const handleCanvasPointerDown = useCallback(
    (e: React.PointerEvent<HTMLDivElement>) => {
      // Only start panning if we're not already dragging a table
      if (draggingTableIdRef.current) return;
      // Only pan on primary button (left click / one finger touch)
      if (e.button !== 0 && e.pointerType === "mouse") return;

      const target = e.target as HTMLElement;
      // Do not activate pan if user clicked inside a card (card has role="button")
      if (target.closest('[data-diagram-card="true"]')) return;

      isPanningRef.current = true;
      panStartRef.current = {
        x: e.clientX - panOffset.x,
        y: e.clientY - panOffset.y,
      };
      (e.currentTarget as HTMLElement).setPointerCapture(e.pointerId);
      setSelectedTableId(null);
      setSelectedRelationshipId(null);
    },
    [panOffset],
  );

  const handleCanvasPointerMove = useCallback((e: React.PointerEvent<HTMLDivElement>) => {
    if (!isPanningRef.current) return;
    setPanOffset({
      x: e.clientX - panStartRef.current.x,
      y: e.clientY - panStartRef.current.y,
    });
  }, []);

  const handleCanvasPointerUp = useCallback(() => {
    isPanningRef.current = false;
  }, []);

  // Wheel zoom handler
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const handleWheel = (e: WheelEvent) => {
      e.preventDefault();
      const delta = e.deltaY > 0 ? -0.08 : 0.08;
      setZoom((z) => Math.min(2.5, Math.max(0.15, z + delta)));
    };

    container.addEventListener("wheel", handleWheel, { passive: false });
    return () => container.removeEventListener("wheel", handleWheel);
  }, []);

  return (
    <div className="flex flex-col h-screen w-full bg-slate-950 text-slate-100 overflow-hidden select-none">
      {/* Toolbar */}
      <DiagramToolbar
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        selectedCategory={selectedCategory}
        onSelectCategory={setSelectedCategory}
        zoom={zoom}
        onZoomIn={handleZoomIn}
        onZoomOut={handleZoomOut}
        onResetZoom={handleResetZoom}
        onResetLayout={handleResetLayout}
        onOpenLegend={() => setIsLegendOpen(true)}
        totalTables={filteredTables.length}
        totalRelationships={visibleRelationships.length}
      />

      {/* Active Relationship Floating Banner */}
      {activeRelationship && (
        <div className="absolute top-16 left-1/2 -translate-x-1/2 z-40 flex items-center gap-3 px-4 py-2.5 rounded-2xl bg-slate-900/95 border border-purple-500/50 shadow-2xl backdrop-blur-md animate-in fade-in slide-in-from-top-2 duration-200">
          <div className="p-2 rounded-xl bg-purple-500/20 text-purple-400">
            <Link2 size={18} />
          </div>
          <div className="text-xs">
            <div className="flex items-center gap-2 font-bold text-slate-100">
              <span className="text-amber-400 font-mono">
                {activeRelationship.fromTable}.{activeRelationship.fromColumn}
              </span>
              <span className="px-2 py-0.5 rounded-md bg-purple-500/30 text-purple-300 font-mono font-extrabold">
                {activeRelationship.cardinality}
              </span>
              <span className="text-blue-400 font-mono">
                {activeRelationship.toTable}.{activeRelationship.toColumn}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-0.5">{activeRelationship.description}</p>
          </div>
          <button
            onClick={() => setSelectedRelationshipId(null)}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X size={16} />
          </button>
        </div>
      )}

      {/* Main Canvas Area */}
      <div
        ref={containerRef}
        role="region"
        aria-label="Lienzo del diagrama relacional"
        tabIndex={0}
        onPointerDown={handleCanvasPointerDown}
        onPointerMove={handleCanvasPointerMove}
        onPointerUp={handleCanvasPointerUp}
        onPointerCancel={handleCanvasPointerUp}
        className="relative flex-1 w-full h-full overflow-hidden bg-slate-950 cursor-grab active:cursor-grabbing bg-[radial-gradient(#1e293b_1px,transparent_1px)] [background-size:24px_24px]"
      >
        {/* Transform container — no transition to avoid drag lag */}
        <div
          style={{
            transform: `translate(${panOffset.x}px, ${panOffset.y}px) scale(${zoom})`,
            transformOrigin: "0 0",
            width: `${CANVAS_WIDTH}px`,
            height: `${CANVAS_HEIGHT}px`,
          }}
          className="absolute inset-0"
        >
          {/* Relationship Lines SVG Overlay */}
          <RelationshipLines
            relationships={visibleRelationships}
            tables={filteredTables}
            positions={positions}
            selectedTableId={selectedTableId}
            selectedRelationshipId={selectedRelationshipId}
            hoveredRelationshipId={hoveredRelationshipId}
            onHoverRelationship={setHoveredRelationshipId}
            onSelectRelationship={(id) => {
              setSelectedRelationshipId(id);
              setSelectedTableId(null);
            }}
          />

          {/* Entity Cards */}
          {filteredTables.map((table) => {
            const pos = positions[table.id] || { x: table.initialX, y: table.initialY };
            const isSelected = selectedTableId === table.id;
            const isConnected = connectedTableIds.has(table.id);

            return (
              <EntityCard
                key={table.id}
                table={table}
                position={pos}
                isSelected={isSelected}
                isHighlighted={isConnected}
                onPointerDown={handleTablePointerDown}
                onPointerMove={handleTablePointerMove}
                onPointerUp={handleTablePointerUp}
                onSelect={(id) => {
                  setSelectedTableId(id);
                  setSelectedRelationshipId(null);
                }}
              />
            );
          })}
        </div>
      </div>

      {/* Legend Modal */}
      <CardinalityLegend isOpen={isLegendOpen} onClose={() => setIsLegendOpen(false)} />
    </div>
  );
}
