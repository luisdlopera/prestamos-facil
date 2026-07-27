import { z } from "zod";

export const loanApplicationSchema = z.object({
  customerId: z.string({ required_error: "El cliente es obligatorio" }).uuid("Cliente inválido"),
  loanTypeId: z
    .string({ required_error: "El tipo de préstamo es obligatorio" })
    .uuid("Tipo de préstamo inválido"),
  requestedAmount: z
    .number({ required_error: "El monto solicitado es obligatorio" })
    .finite("El monto debe ser un número válido")
    .positive("El monto debe ser mayor a 0")
    .max(999_999_999, "El monto excede el límite permitido"),
  termInMonths: z
    .number({ required_error: "El plazo es obligatorio" })
    .finite("El plazo debe ser un número válido")
    .int("El plazo debe ser un número entero")
    .min(1, "El plazo mínimo es 1 mes")
    .max(360, "El plazo máximo es 360 meses (30 años)"),
});

export type LoanApplicationFormData = z.infer<typeof loanApplicationSchema>;
