import { memo } from "react";
import type { Relationship, TableEntity } from "../domain/schema.types";

const CANVAS_WIDTH = 10000;
const CANVAS_HEIGHT = 7000;

interface RelationshipLinesProps {
  relationships: Relationship[];
  tables: TableEntity[];
  positions: Record<string, { x: number; y: number }>;
  selectedTableId: string | null;
  selectedRelationshipId: string | null;
  hoveredRelationshipId: string | null;
  onHoverRelationship: (id: string | null) => void;
  onSelectRelationship: (id: string | null) => void;
}

export const RelationshipLines = memo(function RelationshipLines({
  relationships,
  tables,
  positions,
  selectedTableId,
  selectedRelationshipId,
  hoveredRelationshipId,
  onHoverRelationship,
  onSelectRelationship,
}: RelationshipLinesProps) {
  const tableMap = new Map(tables.map((t) => [t.id, t]));

  return (
    <svg
      width={CANVAS_WIDTH}
      height={CANVAS_HEIGHT}
      viewBox={`0 0 ${CANVAS_WIDTH} ${CANVAS_HEIGHT}`}
      className="absolute inset-0 pointer-events-none z-20 overflow-visible"
    >
      <defs>
        {/* Crow's Foot End Marker (Zero or Many: >o) */}
        <marker
          id="crows-foot-many"
          viewBox="0 0 20 20"
          refX="16"
          refY="10"
          markerWidth="14"
          markerHeight="14"
          orient="auto"
        >
          <circle cx="6" cy="10" r="3" fill="#0f172a" stroke="#38bdf8" strokeWidth="2" />
          <path d="M10 4 L18 10 L10 16" fill="none" stroke="#38bdf8" strokeWidth="2" />
          <line x1="18" y1="4" x2="18" y2="16" stroke="#38bdf8" strokeWidth="2" />
        </marker>

        {/* Crow's Foot End Marker (One or Many: |<) */}
        <marker
          id="crows-foot-one-many"
          viewBox="0 0 20 20"
          refX="16"
          refY="10"
          markerWidth="14"
          markerHeight="14"
          orient="auto"
        >
          <line x1="6" y1="4" x2="6" y2="16" stroke="#38bdf8" strokeWidth="2" />
          <path d="M10 4 L18 10 L10 16" fill="none" stroke="#38bdf8" strokeWidth="2" />
          <line x1="18" y1="4" x2="18" y2="16" stroke="#38bdf8" strokeWidth="2" />
        </marker>

        {/* Exactly One Marker (||) */}
        <marker
          id="marker-one"
          viewBox="0 0 20 20"
          refX="10"
          refY="10"
          markerWidth="12"
          markerHeight="12"
          orient="auto"
        >
          <line x1="6" y1="4" x2="6" y2="16" stroke="#f59e0b" strokeWidth="2" />
          <line x1="12" y1="4" x2="12" y2="16" stroke="#f59e0b" strokeWidth="2" />
        </marker>

        {/* Active Marker */}
        <marker
          id="marker-active"
          viewBox="0 0 20 20"
          refX="16"
          refY="10"
          markerWidth="16"
          markerHeight="16"
          orient="auto"
        >
          <path d="M6 4 L18 10 L6 16 Z" fill="#a855f7" stroke="#c084fc" strokeWidth="1.5" />
        </marker>
      </defs>

      {relationships.map((rel) => {
        const fromPos = positions[rel.fromTable];
        const toPos = positions[rel.toTable];
        const fromTable = tableMap.get(rel.fromTable);
        const toTable = tableMap.get(rel.toTable);

        if (!fromPos || !toPos || !fromTable || !toTable) return null;

        const CARD_WIDTH = 288;
        const HEADER_HEIGHT = 46;
        const ROW_HEIGHT = 29;

        // Port Y calculation mapped to exact PK / FK column row
        const fromColIndex = fromTable.columns.findIndex((c) => c.name === rel.fromColumn);
        const toColIndex = toTable.columns.findIndex((c) => c.name === rel.toColumn);

        const startY =
          fromPos.y +
          HEADER_HEIGHT +
          (fromColIndex >= 0 ? fromColIndex * ROW_HEIGHT + ROW_HEIGHT / 2 : 14.5);
        const endY =
          toPos.y +
          HEADER_HEIGHT +
          (toColIndex >= 0 ? toColIndex * ROW_HEIGHT + ROW_HEIGHT / 2 : 14.5);

        let startX: number, endX: number;

        if (fromPos.x <= toPos.x) {
          // Parent table is to the left (or same column) -> exit right side, enter left side
          startX = fromPos.x + CARD_WIDTH;
          endX = toPos.x;
        } else {
          // Parent table is to the right -> exit left side, enter right side
          startX = fromPos.x;
          endX = toPos.x + CARD_WIDTH;
        }

        // Smooth Bezier Curve Control Points
        const dx = Math.max(70, Math.abs(endX - startX) * 0.45);
        const cp1x = startX < endX ? startX + dx : startX - dx;
        const cp1y = startY;
        const cp2x = startX < endX ? endX - dx : endX + dx;
        const cp2y = endY;

        const pathD = `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`;

        // Midpoint calculation for floating summary label
        const midX = (startX + endX) / 2;
        const midY = (startY + endY) / 2;

        const isRelatedToSelectedTable =
          selectedTableId && (rel.fromTable === selectedTableId || rel.toTable === selectedTableId);
        const isSelectedRel = selectedRelationshipId === rel.id;
        const isHoveredRel = hoveredRelationshipId === rel.id;
        const isActive = isRelatedToSelectedTable || isSelectedRel || isHoveredRel;

        return (
          <g
            key={rel.id}
            className="group pointer-events-auto cursor-pointer"
            onMouseEnter={() => onHoverRelationship(rel.id)}
            onMouseLeave={() => onHoverRelationship(null)}
            onClick={(e) => {
              e.stopPropagation();
              onSelectRelationship(rel.id);
            }}
          >
            {/* Transparent wide path for easy click/hover targeting */}
            <path d={pathD} fill="none" stroke="transparent" strokeWidth="24" />

            {/* Glowing background halo when active */}
            {isActive && (
              <path
                d={pathD}
                fill="none"
                stroke={isSelectedRel ? "#a855f7" : isHoveredRel ? "#3b82f6" : "#60a5fa"}
                strokeWidth="10"
                strokeOpacity="0.45"
              />
            )}

            {/* Connection Line */}
            <path
              d={pathD}
              fill="none"
              stroke={isSelectedRel ? "#a855f7" : isActive ? "#3b82f6" : "#38bdf8"}
              strokeWidth={isSelectedRel ? "3.5" : isActive ? "3" : "2"}
              strokeOpacity={isActive ? "1" : "0.85"}
              strokeDasharray={isSelectedRel ? "10,6" : "none"}
              className={isSelectedRel ? "animate-pulse" : "transition-colors duration-150"}
              markerStart="url(#marker-one)"
              markerEnd={
                isSelectedRel || isActive
                  ? "url(#marker-active)"
                  : rel.toNotation === "1..N"
                    ? "url(#crows-foot-one-many)"
                    : "url(#crows-foot-many)"
              }
            />

            {/* Parent PK Side Badge */}
            <g transform={`translate(${startX + (endX >= startX ? 20 : -20)}, ${startY})`}>
              <rect
                x="-14"
                y="-10"
                width="28"
                height="20"
                rx="6"
                fill="#0f172a"
                stroke={isActive ? "#f59e0b" : "#fbbf24"}
                strokeWidth="1.5"
                className="shadow-md"
              />
              <text x="0" y="4" textAnchor="middle" fontSize="10" fontWeight="800" fill="#fbbf24">
                {rel.fromNotation}
              </text>
            </g>

            {/* Child FK Side Badge */}
            <g transform={`translate(${endX + (startX > endX ? 24 : -24)}, ${endY})`}>
              <rect
                x="-18"
                y="-10"
                width="36"
                height="20"
                rx="6"
                fill="#0f172a"
                stroke={isSelectedRel ? "#c084fc" : isActive ? "#60a5fa" : "#38bdf8"}
                strokeWidth="1.5"
                className="shadow-md"
              />
              <text
                x="0"
                y="4"
                textAnchor="middle"
                fontSize="10"
                fontWeight="800"
                fill={isSelectedRel ? "#e9d5ff" : isActive ? "#93c5fd" : "#38bdf8"}
              >
                {rel.toNotation}
              </text>
            </g>

            {/* Center Relationship Summary Badge on Hover or Selection */}
            {(isHoveredRel || isSelectedRel) && (
              <g transform={`translate(${midX}, ${midY})`}>
                <rect
                  x="-90"
                  y="-14"
                  width="180"
                  height="28"
                  rx="14"
                  fill="#020617"
                  stroke={isSelectedRel ? "#a855f7" : "#3b82f6"}
                  strokeWidth="2"
                  className="shadow-xl"
                />
                <text x="0" y="4" textAnchor="middle" fontSize="11" fontWeight="700" fill="#f8fafc">
                  {rel.cardinality} ({rel.fromTable} → {rel.toTable})
                </text>
              </g>
            )}
          </g>
        );
      })}
    </svg>
  );
});
