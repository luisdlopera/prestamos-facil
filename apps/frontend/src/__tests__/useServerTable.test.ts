import { describe, it, expect, vi } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useServerTable } from "../hooks/useServerTable";
import type { ServerTableParams, ServerTableResult } from "../hooks/useServerTable";

describe("useServerTable hook", () => {
  it("fetches data with default parameters and updates status when handleStatusChange is called", async () => {
    const fetchFnMock = vi.fn(
      async (params: ServerTableParams): Promise<ServerTableResult<{ id: string }>> => {
        return {
          data: [{ id: "1" }],
          pagination: { page: params.page + 1, perPage: params.size, total: 1, totalPages: 1 },
        };
      },
    );

    const { result } = renderHook(() => useServerTable({ fetchFn: fetchFnMock, defaultSize: 10 }));

    // Wait for initial fetch
    await act(async () => {
      await Promise.resolve();
    });

    expect(fetchFnMock).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      search: undefined,
      status: undefined,
      sortBy: undefined,
      sortDir: undefined,
    });

    // Call handleStatusChange
    await act(async () => {
      result.current.handleStatusChange("APPROVED");
    });

    expect(result.current.selectedStatus).toBe("APPROVED");
    expect(fetchFnMock).toHaveBeenLastCalledWith({
      page: 0,
      size: 10,
      search: undefined,
      status: "APPROVED",
      sortBy: undefined,
      sortDir: undefined,
    });
  });
});
