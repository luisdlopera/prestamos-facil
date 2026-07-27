export interface NavItem {
  label: string;
  href: string;
  icon: string;
}

export const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", href: "/dashboard", icon: "📊" },
  { label: "Clientes", href: "/customers", icon: "👥" },
  { label: "Registrar Cliente", href: "/register-customer", icon: "➕" },
  { label: "Solicitudes", href: "/loan-applications", icon: "📋" },
  { label: "Nueva Solicitud", href: "/new-loan-application", icon: "🆕" },
  { label: "Préstamos", href: "/loans", icon: "💰" },
  { label: "Reportes", href: "/reports", icon: "📈" },
];
