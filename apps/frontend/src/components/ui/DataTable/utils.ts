export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

export function filterSourceBySearch<T extends object>(
  data: T[],
  search: string,
  searchableKeys: (keyof T)[],
): T[] {
  const query = search.trim().toLowerCase();
  if (!query) return data;

  return data.filter((row) => {
    return searchableKeys.some((key) => {
      const rawVal = row[key];
      if (rawVal == null) return false;

      let strVal: string;
      if (typeof rawVal === "object") {
        if (Array.isArray(rawVal)) {
          strVal = rawVal.join(" ");
        } else {
          strVal = JSON.stringify(rawVal);
        }
      } else {
        strVal = String(rawVal);
      }

      return strVal.toLowerCase().includes(query);
    });
  });
}

export function getDynamicSearchPlaceholder(searchableColumns: string[]): string {
  if (searchableColumns.length === 0) return "Buscar...";
  if (searchableColumns.length === 1) return `Buscar por ${searchableColumns[0]}...`;
  const lastColumn = searchableColumns[searchableColumns.length - 1];
  const otherColumns = searchableColumns.slice(0, -1);
  return `Buscar por ${otherColumns.join(", ")} o ${lastColumn}...`;
}
