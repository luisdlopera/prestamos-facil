import { Toast } from "@heroui/react";
import { DialogProvider } from "@/components/ui/DialogProvider";
import { AuthProvider } from "@/providers/AuthProvider";
import { PUBLIC_ROUTES } from "@/lib/constants";

interface ProvidersProps {
  children: React.ReactNode;
  currentPath?: string;
}

export function Providers({ children, currentPath }: ProvidersProps) {
  return (
    <AuthProvider publicRoutes={[...PUBLIC_ROUTES]} currentPath={currentPath}>
      <Toast.Provider />
      <DialogProvider>{children}</DialogProvider>
    </AuthProvider>
  );
}
