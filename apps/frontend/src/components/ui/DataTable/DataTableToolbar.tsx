"use client";

import React, { memo, useCallback, useMemo } from "react";
import { Input, Button, Select, ListBox } from "@heroui/react";
import type { Key } from "@heroui/react";
import { Search, X } from "lucide-react";
import { getDynamicSearchPlaceholder } from "./utils";

interface FilterOption {
  label: string;
  value: string;
}

interface DataTableToolbarProps {
  searchValue?: string;
  appliedSearch?: string;
  onSearchValueChange?: (value: string) => void;
  onSearch?: (value: string) => void;
  onClearSearch?: () => void;
  searchableColumns?: string[];
  customSearchPlaceholder?: string;
  statusOptions?: FilterOption[];
  selectedStatus?: string;
  onStatusChange?: (status: string) => void;
  isLoading?: boolean;
}

function DataTableToolbarComponent({
  searchValue = "",
  appliedSearch = "",
  onSearchValueChange,
  onSearch,
  onClearSearch,
  searchableColumns = [],
  customSearchPlaceholder,
  statusOptions,
  selectedStatus,
  onStatusChange,
  isLoading = false,
}: DataTableToolbarProps) {
  const hasSearch = !!onSearch;
  const hasStatusFilter = statusOptions && statusOptions.length > 0;

  const dynamicPlaceholder = useMemo(() => {
    return customSearchPlaceholder || getDynamicSearchPlaceholder(searchableColumns);
  }, [searchableColumns, customSearchPlaceholder]);

  const hasActiveSearch =
    (appliedSearch ?? "").trim().length > 0 || (searchValue ?? "").trim().length > 0;
  const canSearch = (searchValue ?? "").trim().length > 0;

  const currentStatusValue = useMemo(() => {
    return selectedStatus || "ALL";
  }, [selectedStatus]);

  const handleStatusChange = useCallback(
    (key: Key | null) => {
      if (!onStatusChange) return;
      const val = key ? String(key) : "";
      onStatusChange(val === "ALL" ? "" : val);
    },
    [onStatusChange],
  );

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      onSearchValueChange?.(e.target.value);
    },
    [onSearchValueChange],
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter") {
        e.preventDefault();
        const val = (e.currentTarget as HTMLInputElement).value ?? searchValue;
        onSearch?.(val);
      }
    },
    [searchValue, onSearch],
  );

  const handleSearchClick = useCallback(() => {
    onSearch?.(searchValue);
  }, [searchValue, onSearch]);

  const handleClear = useCallback(() => {
    onSearchValueChange?.("");
    onClearSearch?.();
    onSearch?.("");
  }, [onSearchValueChange, onClearSearch, onSearch]);

  if (!hasSearch && !hasStatusFilter) return null;

  return (
    <div className="w-full space-y-4">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        {hasSearch && (
          <div className="flex w-full sm:min-w-[50%] sm:flex-1 items-center gap-2">
            <div className="relative w-full flex-1">
              <Input
                className="w-full pr-8"
                placeholder={dynamicPlaceholder}
                aria-label="Buscar en la tabla"
                value={searchValue}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
              />
              <button
                type="button"
                onClick={handleClear}
                className={`absolute right-2 top-1/2 -translate-y-1/2 flex items-center justify-center transition-opacity ${
                  hasActiveSearch ? "opacity-100" : "opacity-0 pointer-events-none"
                }`}
                aria-label="Limpiar búsqueda"
                tabIndex={hasActiveSearch ? 0 : -1}
              >
                <X className="size-4 text-default-400" />
              </button>
            </div>
            <Button
              variant="ghost"
              size="sm"
              isDisabled={isLoading || !canSearch}
              onPress={handleSearchClick}
              aria-label="Buscar"
              className="font-semibold rounded-lg shrink-0"
            >
              <Search className="size-4" />
            </Button>
          </div>
        )}

        {hasStatusFilter && (
          <div className="flex w-full sm:w-auto gap-2 min-w-[200px]">
            <Select
              className="w-full sm:w-56"
              aria-label="Filtrar por estado"
              placeholder="Filtrar por estado"
              selectedKey={currentStatusValue}
              onSelectionChange={handleStatusChange}
            >
              <Select.Trigger>
                <Select.Value />
                <Select.Indicator />
              </Select.Trigger>
              <Select.Popover>
                <ListBox aria-label="Filtrar por estado">
                  <ListBox.Item id="ALL" textValue="Todos los estados">
                    Todos los estados
                    <ListBox.ItemIndicator />
                  </ListBox.Item>
                  {statusOptions.map((option) => (
                    <ListBox.Item key={option.value} id={option.value} textValue={option.label}>
                      {option.label}
                      <ListBox.ItemIndicator />
                    </ListBox.Item>
                  ))}
                </ListBox>
              </Select.Popover>
            </Select>
          </div>
        )}
      </div>
    </div>
  );
}

export const DataTableToolbar = memo(DataTableToolbarComponent) as typeof DataTableToolbarComponent;
