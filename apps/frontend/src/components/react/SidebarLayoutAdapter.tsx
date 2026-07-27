"use client";

import { useEffect } from "react";
import { useSidebarStore } from "@/lib/stores/sidebar.store";

export function SidebarLayoutAdapter() {
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);

  useEffect(() => {
    const root = document.documentElement;
    root.style.setProperty("--sidebar-width", isCollapsed ? "70px" : "250px");
  }, [isCollapsed]);

  return null;
}
