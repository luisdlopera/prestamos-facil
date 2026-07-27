import { useEffect } from "react";

interface Shortcut {
  key: string;
  ctrl?: boolean;
  alt?: boolean;
  shift?: boolean;
  handler: () => void;
}

export function useKeyboardShortcuts(shortcuts: Shortcut[], enabled = true) {
  useEffect(() => {
    if (!enabled) return;

    const listener = (e: KeyboardEvent) => {
      for (const s of shortcuts) {
        const matchKey = e.key.toLowerCase() === s.key.toLowerCase();
        const matchCtrl = s.ctrl ? e.ctrlKey || e.metaKey : true;
        const matchAlt = s.alt ? e.altKey : true;
        const matchShift = s.shift ? e.shiftKey : true;

        if (matchKey && matchCtrl && matchAlt && matchShift) {
          e.preventDefault();
          s.handler();
          return;
        }
      }
    };

    window.addEventListener("keydown", listener);
    return () => window.removeEventListener("keydown", listener);
  }, [shortcuts, enabled]);
}
