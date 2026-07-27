export const env = {
  apiBaseUrl: import.meta.env.PUBLIC_API_BASE_URL ?? "http://localhost:4010/api/v1",
} as const;
