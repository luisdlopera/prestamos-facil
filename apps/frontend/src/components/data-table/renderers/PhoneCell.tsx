import { Link } from "@heroui/react";
import type { CellRendererProps } from "../types";

const PhoneCell: React.FC<CellRendererProps> = ({ value }) => {
  if (!value) return <span className="text-default-400 select-none">−</span>;

  const phone = String(value);
  return (
    <Link
      href={`tel:${phone}`}
      className="inline-flex items-center gap-1 text-primary text-sm hover:underline"
      aria-label={`Llamar a ${phone}`}
    >
      {phone}
    </Link>
  );
};

export default PhoneCell;
