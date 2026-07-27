import { Link } from "@heroui/react";
import type { CellRendererProps } from "../types";

const EmailCell: React.FC<CellRendererProps> = ({ value }) => {
  if (!value) return <span className="text-default-400 select-none">−</span>;

  const email = String(value);
  return (
    <Link
      href={`mailto:${email}`}
      className="text-primary text-sm hover:underline"
      aria-label={`Enviar email a ${email}`}
    >
      {email}
    </Link>
  );
};

export default EmailCell;
