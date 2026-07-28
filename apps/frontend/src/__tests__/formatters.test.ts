import { describe, it, expect } from "vitest";
import { formatCurrency, formatPercentage } from "../lib/formatters/currency";

describe("formatters", () => {
  it("formats currency in COP", () => {
    const result = formatCurrency(1500000);
    expect(result).toContain("$");
    expect(result).toContain("1.500");
  });

  it("formats percentage", () => {
    const result = formatPercentage(18.5);
    expect(result).toContain("%");
  });
});
