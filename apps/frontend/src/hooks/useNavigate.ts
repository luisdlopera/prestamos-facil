import { useCallback } from "react";

export function useNavigate() {
  const navigate = useCallback((path: string) => {
    if (typeof window !== "undefined" && window.location.pathname !== path) {
      window.location.href = path;
    }
  }, []);

  return navigate;
}
