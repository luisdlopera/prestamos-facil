"use client";

import React, { useCallback } from "react";
import { Button, Select, ListBox } from "@heroui/react";
import type { Key } from "@heroui/react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "./utils";

interface TablePaginationProps {
  page: number;
  totalPages: number;
  totalItems: number;
  pageSize?: number;
  pageSizeOptions?: number[];
  onChange: (page: number) => void;
  onPageSizeChange?: (pageSize: number) => void;
  isLoading?: boolean;
}

function TablePaginationComponent({
  page,
  totalPages,
  totalItems,
  pageSize = DEFAULT_PAGE_SIZE,
  pageSizeOptions = PAGE_SIZE_OPTIONS,
  onChange,
  onPageSizeChange,
  isLoading = false,
}: TablePaginationProps) {
  const startItem = totalItems === 0 ? 0 : (page - 1) * pageSize + 1;
  const endItem = Math.min(page * pageSize, totalItems);

  const handlePrevious = useCallback(() => {
    if (page > 1) onChange(page - 1);
  }, [page, onChange]);

  const handleNext = useCallback(() => {
    if (page < totalPages) onChange(page + 1);
  }, [page, totalPages, onChange]);

  const handlePageSizeChange = useCallback(
    (key: Key | null) => {
      if (key && onPageSizeChange) {
        onPageSizeChange(Number(key));
      }
    },
    [onPageSizeChange],
  );

  const showPageSizeSelector =
    onPageSizeChange && pageSizeOptions.length > 0 && totalItems > pageSize;

  return (
    <div className="flex w-full items-center justify-between gap-4 flex-wrap">
      <div className="flex items-center gap-2 text-sm text-default-500">
        <span>
          Mostrando{" "}
          <span className="font-medium text-default-700">
            {startItem}–{endItem}
          </span>{" "}
          de <span className="font-medium text-default-700">{totalItems}</span>{" "}
          {totalItems === 1 ? "resultado" : "resultados"}
        </span>

        {showPageSizeSelector && (
          <Select
            className="w-28"
            aria-label="Registros por página"
            selectedKey={String(pageSize)}
            onSelectionChange={handlePageSizeChange}
          >
            <Select.Trigger>
              <Select.Value />
              <Select.Indicator />
            </Select.Trigger>
            <Select.Popover>
              <ListBox aria-label="Registros por página">
                {pageSizeOptions.map((size) => (
                  <ListBox.Item key={String(size)} id={String(size)} textValue={`${size} / pág`}>
                    {size} / pág
                    <ListBox.ItemIndicator />
                  </ListBox.Item>
                ))}
              </ListBox>
            </Select.Popover>
          </Select>
        )}
      </div>

      <div className="flex items-center gap-2">
        <Button
          size="sm"
          variant="ghost"
          isDisabled={page <= 1 || isLoading}
          onPress={handlePrevious}
          aria-label="Página anterior"
          className="text-xs"
        >
          <ChevronLeft className="size-4" />
          Anterior
        </Button>
        <span className="text-sm font-medium text-default-600 min-w-[60px] text-center tabular-nums">
          {page} / {totalPages || 1}
        </span>
        <Button
          size="sm"
          variant="ghost"
          isDisabled={page >= totalPages || isLoading}
          onPress={handleNext}
          aria-label="Página siguiente"
          className="text-xs"
        >
          Siguiente
          <ChevronRight className="size-4" />
        </Button>
      </div>
    </div>
  );
}

export const TablePagination = React.memo(
  TablePaginationComponent,
) as typeof TablePaginationComponent;
