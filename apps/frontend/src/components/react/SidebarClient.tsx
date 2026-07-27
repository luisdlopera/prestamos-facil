import { useMemo, useCallback } from "react";

import {
  LayoutDashboard,
  Users,
  UserPlus,
  FileText,
  FilePlus,
  Wallet,
  BarChart3,
  ChevronLeft,
  ChevronRight,
  Landmark,
} from "lucide-react";
import { useAuthStore, checkIsAdmin } from "@/lib/stores/auth.store";
import { useSidebarStore } from "@/lib/stores/sidebar.store";

interface MenuItem {
  href: string;
  label: string;
  Icon: React.ElementType;
  section: "Principal" | "Operaciones" | "Administración";
  roles?: ("customer" | "staff" | "admin")[];
}

const allMenuItems: MenuItem[] = [
  {
    href: "/dashboard",
    label: "Dashboard",
    Icon: LayoutDashboard,
    section: "Principal",
    roles: ["staff"],
  },
  {
    href: "/customers",
    label: "Clientes",
    Icon: Users,
    section: "Operaciones",
    roles: ["staff"],
  },
  {
    href: "/register-customer",
    label: "Registrar Cliente",
    Icon: UserPlus,
    section: "Operaciones",
    roles: ["staff"],
  },
  {
    href: "/loan-applications",
    label: "Solicitudes",
    Icon: FileText,
    section: "Operaciones",
    roles: ["staff"],
  },
  {
    href: "/my-loan-applications",
    label: "Mis Solicitudes",
    Icon: FileText,
    section: "Operaciones",
    roles: ["customer"],
  },
  {
    href: "/new-loan-application",
    label: "Nueva Solicitud",
    Icon: FilePlus,
    section: "Operaciones",
    roles: ["customer", "staff"],
  },
  {
    href: "/loans",
    label: "Préstamos",
    Icon: Wallet,
    section: "Operaciones",
    roles: ["staff"],
  },
  {
    href: "/my-loans",
    label: "Mis Préstamos",
    Icon: Wallet,
    section: "Operaciones",
    roles: ["customer"],
  },
  {
    href: "/reports",
    label: "Reportes",
    Icon: BarChart3,
    section: "Administración",
    roles: ["admin"],
  },
  {
    href: "/loan-types",
    label: "Tipos de Crédito",
    Icon: Landmark,
    section: "Administración",
    roles: ["admin"],
  },
];

export function SidebarClient() {
  const user = useAuthStore((s) => s.user);
  const userType = useAuthStore((s) => s.userType);
  const currentPath = typeof window !== "undefined" ? window.location.pathname : "";
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);
  const toggleCollapsed = useSidebarStore((s) => s.toggleCollapsed);
  const isMobileOpen = useSidebarStore((s) => s.isMobileOpen);
  const setMobileOpen = useSidebarStore((s) => s.setMobileOpen);

  const closeMobile = useCallback(() => {
    setMobileOpen(false);
  }, [setMobileOpen]);

  const isAdmin = useMemo(() => checkIsAdmin(user, userType), [user, userType]);
  const isStaff = useMemo(
    () =>
      userType === "staff" ||
      isAdmin ||
      (user?.roles &&
        user.roles.some((r) =>
          ["STAFF", "ANALYST", "ASESOR", "COLLECTOR", "ADMIN"].includes(r.toUpperCase()),
        )),
    [userType, isAdmin, user],
  );

  const visibleItems = useMemo(
    () =>
      allMenuItems.filter((item) => {
        if (!item.roles) return true;
        return item.roles.some((role) => {
          if (role === "admin") return isAdmin;
          if (role === "staff") return isStaff;
          if (role === "customer") return !isStaff;
          return false;
        });
      }),
    [isAdmin, isStaff],
  );

  const sections = useMemo(() => {
    const list: { name: string; items: MenuItem[] }[] = [];
    const sectionNames: ("Principal" | "Operaciones" | "Administración")[] = [
      "Principal",
      "Operaciones",
      "Administración",
    ];

    for (const sec of sectionNames) {
      const items = visibleItems.filter((i) => i.section === sec);
      if (items.length > 0) {
        list.push({ name: sec, items });
      }
    }
    return list;
  }, [visibleItems]);

  const handleToggle = useCallback(() => {
    toggleCollapsed();
  }, [toggleCollapsed]);

  return (
    <>
      {/* Mobile Backdrop Overlay */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs md:hidden"
          onClick={closeMobile}
          aria-hidden="true"
        />
      )}

      <aside
        className={`sidebar ${isCollapsed ? "sidebar--collapsed" : ""} ${
          isMobileOpen ? "sidebar--mobile-open" : ""
        }`}
      >
        <div className="sidebar-header">
          <div className="sidebar-brand">
            <div className="brand-icon">
              <Landmark className="size-5 text-white" />
            </div>
            {(!isCollapsed || isMobileOpen) && (
              <div className="brand-text">
                <span className="brand-title">Préstamos Fácil</span>
                <span className="brand-subtitle">Panel Financiero</span>
              </div>
            )}
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={handleToggle}
              className="sidebar-toggle hidden md:flex"
              aria-label={isCollapsed ? "Expandir menú" : "Contraer menú"}
            >
              {isCollapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
            </button>
          </div>
        </div>

        <nav className="sidebar-nav">
          {sections.map((sec) => (
            <div key={sec.name} className="sidebar-section">
              {(!isCollapsed || isMobileOpen) && <div className="section-title">{sec.name}</div>}
              <ul>
                {sec.items.map(({ href, label, Icon }) => {
                  const isActive = currentPath === href;
                  return (
                    <li key={href}>
                      <a
                        href={href}
                        onClick={closeMobile}
                        className={`sidebar-link ${isActive ? "sidebar-link--active" : ""}`}
                        title={isCollapsed && !isMobileOpen ? label : undefined}
                        aria-current={isActive ? "page" : undefined}
                      >
                        <span className={`icon ${isActive ? "icon--active" : ""}`}>
                          <Icon size={18} />
                        </span>
                        {(!isCollapsed || isMobileOpen) && <span className="label">{label}</span>}
                      </a>
                    </li>
                  );
                })}
              </ul>
            </div>
          ))}
        </nav>
      </aside>
    </>
  );
}
