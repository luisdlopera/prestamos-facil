"use client";

import { useState, useCallback, useMemo } from "react";
import type { Key } from "@heroui/react";
import {
  Button,
  Card,
  FieldError,
  Input,
  InputGroup,
  Label,
  ListBox,
  Select,
  TextField,
} from "@heroui/react";
import { Eye, EyeOff } from "lucide-react";
import { post } from "@/lib/api/client";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useAsyncAction } from "@/lib/errors";
import { registerSchema } from "../schemas/register.schema";

const DOCUMENT_TYPES = [
  { key: "CC", label: "Cédula de Ciudadanía" },
  { key: "CE", label: "Cédula de Extranjería" },
  { key: "NIT", label: "NIT" },
  { key: "TI", label: "Tarjeta de Identidad" },
  { key: "PP", label: "Pasaporte" },
];

export function RegisterPage() {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    documentType: "CC",
    documentNumber: "",
    baseSalary: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const clearUser = useAuthStore((s) => s.clearUser);

  const { run, isLoading } = useAsyncAction({
    successMsg: "Registro exitoso",
    onSuccess: () => {
      // /auth/register no crea sesión: el backend no devuelve access token ni refresh cookie.
      // El usuario debe autenticarse explícitamente en el login.
      clearUser();
      window.location.href = "/login?registered=1";
    },
  });

  const handleSubmit = useCallback(
    (e: React.SyntheticEvent<HTMLFormElement>) => {
      e.preventDefault();
      setErrors({});

      const result = registerSchema.safeParse(formData);
      if (!result.success) {
        const fieldErrors: Record<string, string> = {};
        result.error.errors.forEach((error) => {
          const field = error.path[0];
          fieldErrors[field as string] = error.message;
        });
        setErrors(fieldErrors);
        return;
      }

      run(async () => {
        await post("/auth/register", {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          documentType: formData.documentType,
          documentNumber: formData.documentNumber,
          baseSalary: parseFloat(formData.baseSalary),
          password: formData.password,
        });
      });
    },
    [formData, run],
  );

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
    setErrors((prev) => {
      if (!prev[id]) return prev;
      const copy = { ...prev };
      delete copy[id];
      return copy;
    });
  }, []);

  const handleDocumentTypeChange = useCallback((key: Key | null) => {
    if (key) {
      const val = String(key);
      setFormData((prev) => ({ ...prev, documentType: val }));
      setErrors((prev) => {
        if (!prev.documentType) return prev;
        const copy = { ...prev };
        delete copy.documentType;
        return copy;
      });
    }
  }, []);

  const toggleShowPassword = useCallback(() => {
    setShowPassword((prev) => !prev);
  }, []);

  const documentTypeOptions = useMemo(() => {
    return DOCUMENT_TYPES.map((type) => (
      <ListBox.Item
        key={type.key}
        id={type.key}
        textValue={type.label}
        className="text-black hover:bg-blue-100"
      >
        {type.label}
        <ListBox.ItemIndicator />
      </ListBox.Item>
    ));
  }, []);

  return (
    <Card className="w-full max-w-md border-0 shadow-lg">
      <Card.Header>
        <Card.Title className="text-xl">Registrarse</Card.Title>
        <Card.Description>Cree una cuenta para solicitar préstamos</Card.Description>
      </Card.Header>
      <Card.Content>
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="grid grid-cols-2 gap-2">
            <TextField isInvalid={!!errors.firstName} className="space-y-1">
              <Label className="text-sm font-medium text-black">
                Nombres <span className="text-red-600">*</span>
              </Label>
              <Input
                id="firstName"
                placeholder="Ingrese su nombre"
                value={formData.firstName}
                onChange={handleInputChange}
              />
              <FieldError className="text-xs text-red-600">{errors.firstName}</FieldError>
            </TextField>
            <TextField isInvalid={!!errors.lastName} className="space-y-1">
              <Label className="text-sm font-medium text-black">
                Apellidos <span className="text-red-600">*</span>
              </Label>
              <Input
                id="lastName"
                placeholder="Ingrese su apellido"
                value={formData.lastName}
                onChange={handleInputChange}
              />
              <FieldError className="text-xs text-red-600">{errors.lastName}</FieldError>
            </TextField>
          </div>

          <TextField isInvalid={!!errors.email} className="space-y-1">
            <Label className="text-sm font-medium text-black">
              Correo electrónico <span className="text-red-600">*</span>
            </Label>
            <Input
              id="email"
              type="email"
              placeholder="Ingrese su correo electrónico"
              value={formData.email}
              onChange={handleInputChange}
            />
            <FieldError className="text-xs text-red-600">{errors.email}</FieldError>
          </TextField>

          <div className="grid grid-cols-2 gap-2">
            <div className="space-y-1">
              <Select
                fullWidth
                placeholder="Seleccionar..."
                selectedKey={formData.documentType}
                onSelectionChange={handleDocumentTypeChange}
              >
                <Label className="text-sm font-medium text-black">
                  Tipo documento <span className="text-red-600">*</span>
                </Label>
                <Select.Trigger>
                  <Select.Value />
                  <Select.Indicator />
                </Select.Trigger>
                <Select.Popover>
                  <ListBox>{documentTypeOptions}</ListBox>
                </Select.Popover>
              </Select>
              {errors.documentType && <p className="text-xs text-red-600">{errors.documentType}</p>}
            </div>
            <TextField isInvalid={!!errors.documentNumber} className="space-y-1">
              <Label className="text-sm font-medium text-black">
                Número <span className="text-red-600">*</span>
              </Label>
              <Input
                id="documentNumber"
                placeholder="Ingrese su número"
                value={formData.documentNumber}
                onChange={handleInputChange}
              />
              <FieldError className="text-xs text-red-600">{errors.documentNumber}</FieldError>
            </TextField>
          </div>

          <TextField isInvalid={!!errors.baseSalary} className="space-y-1">
            <Label className="text-sm font-medium text-black">
              Salario base (COP) <span className="text-red-600">*</span>
            </Label>
            <Input
              id="baseSalary"
              type="number"
              placeholder="Ingrese el salario"
              value={formData.baseSalary}
              onChange={handleInputChange}
            />
            <FieldError className="text-xs text-red-600">{errors.baseSalary}</FieldError>
          </TextField>

          <TextField isInvalid={!!errors.password} className="space-y-1">
            <Label className="text-sm font-medium text-black">
              Contraseña <span className="text-red-600">*</span>
            </Label>
            <InputGroup>
              <InputGroup.Input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                value={formData.password}
                onChange={handleInputChange}
              />
              <InputGroup.Suffix className="pr-0">
                <Button
                  isIconOnly
                  aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                  size="sm"
                  variant="ghost"
                  onPress={toggleShowPassword}
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </Button>
              </InputGroup.Suffix>
            </InputGroup>
            <FieldError className="text-xs text-red-600">{errors.password}</FieldError>
          </TextField>

          <Button type="submit" fullWidth isPending={isLoading}>
            Registrarse
          </Button>
        </form>
        <div className="mt-4 text-center">
          <a href="/login" className="text-blue-600 hover:underline text-sm">
            ¿Ya tiene cuenta? Inicie sesión
          </a>
        </div>
      </Card.Content>
    </Card>
  );
}
