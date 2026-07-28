"use client";

import { useEffect, useState } from "react";

/**
 * Returns true once `isLoading` has stayed true for `delayMs`.
 * Used to show a "waking up the server" hint during Render free-tier
 * cold starts, without flashing the hint on normal fast requests.
 */
export function useSlowRequestHint(isLoading: boolean, delayMs = 5000): boolean {
  const [isSlow, setIsSlow] = useState(false);

  useEffect(() => {
    if (!isLoading) {
      setIsSlow(false);
      return;
    }
    const timeoutId = setTimeout(() => setIsSlow(true), delayMs);
    return () => clearTimeout(timeoutId);
  }, [isLoading, delayMs]);

  return isSlow;
}
