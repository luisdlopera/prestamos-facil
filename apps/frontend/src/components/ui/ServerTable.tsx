"use client";

import { useMemo } from "react";
import { Table, Skeleton } from "@heroui/react";
import type { SortDescriptor } from "@heroui/react";
import { DataTableCellRenderer } from "@/components/data-table";
import type { DataTableColumn } from "@/components/data-table";
import { useServerTable } from "@/hooks/useServerTable";
import type { ServerTableFetchFn } from "@/hooks/useServerTable";
import { DataTableToolbar } from "@/components/ui/DataTable/DataTableToolbar";
import { TablePagination } from "@/components/ui/DataTable/TablePagination";
import { ErrorState } from "@/components/ui/ErrorState";

export interface ServerTableColumn<T> extends DataTableColumn<T> {
  key: keyof T | (string & {});
  searchable?: boolean;
}

export interface StatusOption {
  label: string;
  value: string;
}

export interface ServerTableProps<T> {
  columns: ServerTableColumn<T>[];
  fetchFn: ServerTableFetchFn<T>;
  defaultSize?: number;
  defaultSort?: SortDescriptor;
  defaultStatus?: string;
  defaultSearch?: string;
  deps?: unknown[];
  searchable?: boolean;
  searchPlaceholder?: string;
  emptyMessage?: string;
  statusOptions?: StatusOption[];
  onStatusChange?: (status: string) => void;
  selectedStatus?: string;
  debounceMs?: number;
}

export function ServerTable<T>({
  columns,
  fetchFn,
  defaultSize = 10,
  defaultSort,
  defaultStatus,
  defaultSearch,
  deps,
  searchable: _searchable = true,
  searchPlaceholder = "Buscar...",
  emptyMessage = "No hay registros",
  statusOptions,
  onStatusChange: externalOnStatusChange,
  selectedStatus: externalSelectedStatus,
  debounceMs,
}: ServerTableProps<T>) {
  const {
    rows,
    total,
    totalPages,
    page,
    size,
    inputValue,
    selectedStatus: internalSelectedStatus,
    isLoading,
    error,
    handleSearchValueChange,
    handleSearch,
    handleClearSearch,
    handleStatusChange: internalHandleStatusChange,
    handlePageChange,
    handlePageSizeChange,
    handleSortChange,
    reload,
  } = useServerTable<T>({
    fetchFn,
    defaultSize,
    defaultSort,
    defaultStatus: externalSelectedStatus ?? defaultStatus,
    defaultSearch,
    deps,
    debounceMs,
  });

  const activeSelectedStatus =
    externalSelectedStatus !== undefined ? externalSelectedStatus : internalSelectedStatus;

  const handleStatusChange = useMemo(() => {
    return (status: string) => {
      internalHandleStatusChange(status);
      externalOnStatusChange?.(status);
    };
  }, [internalHandleStatusChange, externalOnStatusChange]);

  const visibleColumns = useMemo(() => {
    return columns.filter((col) => String(col.key).toLowerCase() !== "id");
  }, [columns]);

  const firstColumnKey = String(visibleColumns[0]?.key ?? "id");

  const searchableColumns = useMemo(() => {
    return visibleColumns
      .filter((col) => col.searchable !== false)
      .map((col) => col.header ?? col.label ?? String(col.key));
  }, [visibleColumns]);

  return (
    <div className="space-y-4">
      {/* Toolbar con búsqueda y filtros */}
      <DataTableToolbar
        searchValue={inputValue}
        appliedSearch={inputValue}
        onSearchValueChange={handleSearchValueChange}
        onSearch={handleSearch}
        onClearSearch={handleClearSearch}
        searchableColumns={searchableColumns}
        customSearchPlaceholder={searchPlaceholder}
        statusOptions={statusOptions}
        selectedStatus={activeSelectedStatus}
        onStatusChange={handleStatusChange}
        isLoading={isLoading}
      />

      {/* Error state */}
      {error && <ErrorState message={error} onRetry={reload} />}

      {/* Table */}
      <div className="rounded-lg">
        <Table>
          <Table.ScrollContainer>
            <Table.Content
              aria-label="Tabla de datos"
              className="min-w-[700px]"
              sortDescriptor={
                defaultSort
                  ? {
                      column: defaultSort.column,
                      direction: defaultSort.direction as "ascending" | "descending",
                    }
                  : undefined
              }
              onSortChange={handleSortChange}
            >
              <Table.Header>
                {visibleColumns.map((col) => (
                  <Table.Column
                    key={String(col.key)}
                    id={String(col.key)}
                    allowsSorting={col.sortable}
                    isRowHeader={String(col.key) === firstColumnKey}
                  >
                    {({ sortDirection }: { sortDirection?: "ascending" | "descending" }) =>
                      col.sortable ? (
                        <Table.SortableColumnHeader sortDirection={sortDirection}>
                          {col.header ?? col.label ?? String(col.key)}
                        </Table.SortableColumnHeader>
                      ) : (
                        (col.header ?? col.label ?? String(col.key))
                      )
                    }
                  </Table.Column>
                ))}
              </Table.Header>
              <Table.Body>
                {isLoading && rows.length === 0 ? (
                  <Table.Row>
                    <Table.Cell colSpan={visibleColumns.length}>
                      <div className="space-y-3 py-4 px-6">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <div key={i} className="flex items-center gap-4">
                            {visibleColumns.map((col) => (
                              <Skeleton key={String(col.key)} className="h-5 flex-1 rounded-lg" />
                            ))}
                          </div>
                        ))}
                      </div>
                    </Table.Cell>
                  </Table.Row>
                ) : rows.length === 0 ? (
                  <Table.Row>
                    <Table.Cell colSpan={visibleColumns.length}>
                      <div className="flex h-full w-full flex-col items-center justify-center gap-2 py-12 text-center">
                        <span className="text-sm text-default-500">{emptyMessage}</span>
                      </div>
                    </Table.Cell>
                  </Table.Row>
                ) : (
                  rows.map((row, idx) => {
                    const record = row as Record<string, unknown>;
                    const rowKey = String(record.id ?? record[firstColumnKey] ?? idx);
                    return (
                      <Table.Row key={rowKey}>
                        {visibleColumns.map((col) => {
                          const cellValue = (row as Record<string, unknown>)[String(col.key)];
                          return (
                            <Table.Cell key={String(col.key)}>
                              <DataTableCellRenderer
                                column={col as DataTableColumn<Record<string, unknown>>}
                                value={cellValue}
                                row={row as Record<string, unknown>}
                              />
                            </Table.Cell>
                          );
                        })}
                      </Table.Row>
                    );
                  })
                )}
              </Table.Body>
            </Table.Content>
          </Table.ScrollContainer>
        </Table>
      </div>

      {/* Pagination */}
      {total > 0 && (
        <TablePagination
          page={page}
          totalPages={totalPages}
          totalItems={total}
          pageSize={size}
          onChange={handlePageChange}
          onPageSizeChange={handlePageSizeChange}
          isLoading={isLoading}
        />
      )}
    </div>
  );
}
