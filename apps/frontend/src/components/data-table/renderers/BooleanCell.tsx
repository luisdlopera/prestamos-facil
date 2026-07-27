import { Chip } from "@heroui/react";
import type { CellRendererProps } from "../types";

const BooleanCell: React.FC<CellRendererProps> = ({ value }) => {
  const isActive = Boolean(value);

  return (
    <Chip color={isActive ? "success" : "default"} size="sm" variant="soft">
      {isActive ? "Sí" : "No"}
    </Chip>
  );
};

export default BooleanCell;
