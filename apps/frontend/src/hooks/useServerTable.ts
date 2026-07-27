import { useState, useCallback, useRef, useEffect } from "react";
import type { SortDescriptor } from "@heroui/react";

export interface ServerTableParams {
  page: number;
  size: number;
  search?: string;
  sortBy?: string;
  sortDir?: "asc" | "desc";
  status?: string;
}

export interface ServerTableResult<T> {
  data: T[];
  pagination: {
    page: number;
    perPage: number;
    total: number;
    totalPages: number;
  };
}

export interface ServerTableFetchFn<T> {
  (params: ServerTableParams): Promise<ServerTableResult<T>>;
}

export interface UseServerTableOptions<T> {
  fetchFn: ServerTableFetchFn<T>;
  defaultSize?: number;
  defaultSort?: SortDescriptor;
  defaultStatus?: string;
  defaultSearch?: string;
  deps?: unknown[];
  debounceMs?: number;
}

export function useServerTable<T>({
  fetchFn,
  defaultSize = 10,
  defaultSort,
  defaultStatus = "",
  defaultSearch = "",
  deps = [],
  debounceMs,
}: UseServerTableOptions<T>) {
  const [page, setPageState] = useState(1);
  const [size, setSize] = useState(defaultSize);
  const [inputValue, setInputValue] = useState(defaultSearch);
  const [appliedSearch, setAppliedSearch] = useState(defaultSearch);
  const [selectedStatus, setSelectedStatus] = useState(defaultStatus);
  const [sortDescriptor, setSortDescriptor] = useState<SortDescriptor | undefined>(defaultSort);
  const [rows, setRows] = useState<T[]>([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const fetchFnRef = useRef(fetchFn);
  fetchFnRef.current = fetchFn;

  const mountedRef = useRef(false);

  const doFetch = useCallback(
    async (p: number, s: number, search: string, status: string, sort?: SortDescriptor) => {
      setIsLoading(true);
      setError(null);
      try {
        const params: ServerTableParams = {
          page: p - 1,
          size: s,
          search: search || undefined,
          status: status || undefined,
          sortBy: sort?.column as string | undefined,
          sortDir:
            sort?.direction === "ascending"
              ? "asc"
              : sort?.direction === "descending"
                ? "desc"
                : undefined,
        };
        const result = await fetchFnRef.current(params);
        if (mountedRef.current) {
          setRows(result.data ?? []);
          setTotal(result.pagination.total ?? 0);
          setTotalPages(result.pagination.totalPages ?? 0);
        }
      } catch (e) {
        const msg = e instanceof Error ? e.message : "Error al cargar datos";
        if (mountedRef.current) {
          setError(msg);
          setRows([]);
          setTotal(0);
          setTotalPages(0);
        }
      } finally {
        if (mountedRef.current) {
          setIsLoading(false);
        }
      }
    },
    [],
  );

  const stableDoFetch = useRef(doFetch);
  stableDoFetch.current = doFetch;

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
    };
  }, []);

  useEffect(() => {
    stableDoFetch.current(page, size, appliedSearch, selectedStatus, sortDescriptor);
  }, [page, size, appliedSearch, selectedStatus, sortDescriptor, ...deps]);

  const setPage = useCallback((p: number) => setPageState(p), []);

  const handleSearch = useCallback(
    (value: string) => {
      if (debounceMs && debounceMs > 0) {
        if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
        debounceTimerRef.current = setTimeout(() => {
          setAppliedSearch(value);
          setPageState(1);
        }, debounceMs);
      } else {
        setAppliedSearch(value);
        setPageState(1);
      }
    },
    [debounceMs],
  );

  const handleSearchValueChange = useCallback(
    (value: string) => {
      setInputValue(value);
      if (debounceMs && debounceMs > 0) {
        if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
        debounceTimerRef.current = setTimeout(() => {
          setAppliedSearch(value);
          setPageState(1);
        }, debounceMs);
      }
    },
    [debounceMs],
  );

  const handleClearSearch = useCallback(() => {
    if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
    setInputValue("");
    setAppliedSearch("");
    setPageState(1);
  }, []);

  const handleStatusChange = useCallback((newStatus: string) => {
    setSelectedStatus(newStatus);
    setPageState(1);
  }, []);

  const handlePageChange = useCallback(
    (newPage: number, newSize?: number) => {
      setPageState(newPage);
      if (newSize && newSize !== size) {
        setSize(newSize);
        setPageState(1);
      }
    },
    [size],
  );

  const handleSortChange = useCallback((desc: SortDescriptor) => {
    setSortDescriptor(desc);
    setPageState(1);
  }, []);

  const handlePageSizeChange = useCallback((newSize: number) => {
    setSize(newSize);
    setPageState(1);
  }, []);

  const reload = useCallback(() => {
    stableDoFetch.current(page, size, appliedSearch, selectedStatus, sortDescriptor);
  }, [page, size, appliedSearch, selectedStatus, sortDescriptor]);

  return {
    rows,
    total,
    totalPages,
    page,
    size,
    inputValue,
    appliedSearch,
    selectedStatus,
    sortDescriptor,
    isLoading,
    error,
    setPage,
    setInputValue,
    setSelectedStatus,
    handleSearchValueChange,
    handleSearch,
    handleClearSearch,
    handleStatusChange,
    handlePageChange,
    handleSortChange,
    handlePageSizeChange,
    reload,
  };
}
