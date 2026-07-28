import { z } from "zod";

const TEMPORARY_EMAIL_DOMAINS = [
  "yopmail.com",
  "guerrillamail.com",
  "mailinator.com",
  "temp-mail.org",
  "throwaway.email",
  "10minutemail.com",
  "tempmail.com",
  "fakeinbox.com",
  "trashmail.com",
  "dispostable.com",
];

export const registerSchema = z.object({
  firstName: z.string().min(2, "El nombre debe tener al menos 2 caracteres").max(100),
  lastName: z.string().min(2, "El apellido debe tener al menos 2 caracteres").max(100),
  email: z
    .string()
    .email("Correo electrónico inválido")
    .refine((email) => {
      const domain = email.split("@")[1]?.toLowerCase();
      return !TEMPORARY_EMAIL_DOMAINS.includes(domain);
    }, "No se permiten servicios de correo temporal"),
  documentType: z.enum(["CC", "CE", "NIT", "TI", "PP", "PEP"], {
    required_error: "Seleccione un tipo de documento",
  }),
  documentNumber: z.string().min(3, "Número de documento inválido").max(50),
  baseSalary: z.string().refine((val) => {
    const num = parseFloat(val);
    return Number.isFinite(num) && num >= 0 && num <= 15000000;
  }, "El salario debe estar entre 0 y 15,000,000"),
  password: z
    .string()
    .min(8, "La contraseña debe tener al menos 8 caracteres")
    .regex(/[A-Z]/, "La contraseña debe incluir una mayúscula")
    .regex(/[0-9]/, "La contraseña debe incluir un número")
    .regex(/[!@#$%^&+=]/, "La contraseña debe incluir un carácter especial (!@#$%^&+=)"),
});

export type RegisterFormData = z.infer<typeof registerSchema>;
