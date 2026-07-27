import { z } from "zod";

export const customerSchema = z.object({
  firstName: z
    .string({ required_error: "El nombre es obligatorio" })
    .min(2, "El nombre debe tener al menos 2 caracteres")
    .max(100, "El nombre no puede exceder 100 caracteres"),
  lastName: z
    .string({ required_error: "Los apellidos son obligatorios" })
    .min(2, "Los apellidos deben tener al menos 2 caracteres")
    .max(100, "Los apellidos no pueden exceder 100 caracteres"),
  email: z
    .string({ required_error: "El correo electrónico es obligatorio" })
    .email("El formato del correo electrónico no es válido")
    .max(255, "El correo no puede exceder 255 caracteres"),
  documentType: z.enum(["CC", "CE", "NIT", "TI", "PP", "PEP"], {
    required_error: "El tipo de documento es obligatorio",
  }),
  documentNumber: z
    .string({ required_error: "El número de documento es obligatorio" })
    .min(3, "El número de documento debe tener al menos 3 caracteres")
    .max(20, "El número de documento no puede exceder 20 caracteres"),
  baseSalary: z
    .number({ required_error: "El salario base es obligatorio" })
    .finite("El salario debe ser un número válido")
    .min(0, "El salario no puede ser negativo")
    .max(15_000_000, "El salario no puede exceder $15.000.000"),
});

export type CustomerFormData = z.infer<typeof customerSchema>;
