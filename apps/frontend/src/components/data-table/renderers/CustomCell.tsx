import type { CellRendererProps } from "../types";
import TextCell from "./TextCell";

const CustomCell: React.FC<CellRendererProps> = (props) => {
  const { column, row } = props;

  if (typeof column.render === "function") {
    return <>{column.render(row)}</>;
  }

  return <TextCell {...props} />;
};

export default CustomCell;
