"use client";

import { useEffect, useState } from "react";
import { Button, Card, FieldError, Input, InputGroup, Label, TextField } from "@heroui/react";
import { Eye, EyeOff, KeyRound, Save, UserRound } from "lucide-react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useAsyncAction } from "@/lib/errors";
import {
  changePassword,
  fetchProfile,
  updateProfile,
  type AuthUserProfile,
} from "../infrastructure/auth-service";

export function ProfilePage() {
  const setUser = useAuthStore((s) => s.setUser);
  const [profile, setProfile] = useState<AuthUserProfile | null>(null);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    baseSalary: "",
    email: "",
  });
  const [passwordData, setPasswordData] = useState({ currentPassword: "", newPassword: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);

  useEffect(() => {
    fetchProfile().then((data) => {
      setProfile(data);
      setFormData({
        firstName: data.firstName,
        lastName: data.lastName,
        baseSalary: String(data.baseSalary),
        email: data.email,
      });
    });
  }, []);

  const { run: runUpdate, isLoading: isSavingProfile } = useAsyncAction<AuthUserProfile>({
    successMsg: "Perfil actualizado exitosamente",
    onSuccess: (updated) => {
      setProfile(updated);
      setUser({
        id: updated.id,
        name: `${updated.firstName} ${updated.lastName}`,
        email: updated.email,
        roles: [],
      });
    },
  });

  const { run: runChangePassword, isLoading: isSavingPassword } = useAsyncAction({
    successMsg: "Contraseña actualizada exitosamente",
    onSuccess: () => setPasswordData({ currentPassword: "", newPassword: "" }),
  });

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
    setErrors((prev) => {
      if (!prev[id]) return prev;
      const copy = { ...prev };
      delete copy[id];
      return copy;
    });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setPasswordData((prev) => ({ ...prev, [id]: value }));
  };

  const handleProfileSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};
    if (formData.firstName.trim().length < 2) newErrors.firstName = "Mínimo 2 caracteres";
    if (formData.lastName.trim().length < 2) newErrors.lastName = "Mínimo 2 caracteres";
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email.trim())) newErrors.email = "Correo electrónico inválido";
    const salary = Number(formData.baseSalary);
    if (Number.isNaN(salary) || salary < 0 || salary > 15000000) {
      newErrors.baseSalary = "El salario debe estar entre 0 y 15.000.000";
    }
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    runUpdate(() =>
      updateProfile({
        firstName: formData.firstName,
        lastName: formData.lastName,
        baseSalary: salary,
        email: formData.email.trim(),
      }),
    );
  };

  const handlePasswordSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    runChangePassword(() => changePassword(passwordData));
  };

  if (!profile) {
    return <p className="text-sm text-muted">Cargando perfil...</p>;
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <Card className="w-full">
        <Card.Header>
          <Card.Title className="flex items-center gap-2 text-lg">
            <UserRound className="size-5" />
            Mi Perfil
          </Card.Title>
          <Card.Description>Actualice sus datos personales</Card.Description>
        </Card.Header>
        <Card.Content>
          <form onSubmit={handleProfileSubmit} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <TextField isInvalid={!!errors.firstName} className="space-y-1">
                <Label className="text-sm font-medium">Nombres</Label>
                <Input id="firstName" value={formData.firstName} onChange={handleProfileChange} />
                <FieldError className="text-xs text-red-600">{errors.firstName}</FieldError>
              </TextField>
              <TextField isInvalid={!!errors.lastName} className="space-y-1">
                <Label className="text-sm font-medium">Apellidos</Label>
                <Input id="lastName" value={formData.lastName} onChange={handleProfileChange} />
                <FieldError className="text-xs text-red-600">{errors.lastName}</FieldError>
              </TextField>
            </div>

            <TextField isInvalid={!!errors.email} className="space-y-1">
              <Label className="text-sm font-medium">Correo electrónico</Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={handleProfileChange}
              />
              <FieldError className="text-xs text-red-600">{errors.email}</FieldError>
            </TextField>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="space-y-1">
                <Label className="text-sm font-medium">Tipo de documento</Label>
                <Input value={profile.documentType || ""} disabled />
              </div>
              <div className="space-y-1">
                <Label className="text-sm font-medium">Número de documento</Label>
                <Input value={profile.documentNumber || ""} disabled />
              </div>
            </div>

            <TextField isInvalid={!!errors.baseSalary} className="space-y-1">
              <Label className="text-sm font-medium">Salario base (COP)</Label>
              <Input
                id="baseSalary"
                type="number"
                value={formData.baseSalary}
                onChange={handleProfileChange}
              />
              <FieldError className="text-xs text-red-600">{errors.baseSalary}</FieldError>
            </TextField>

            <Button type="submit" isPending={isSavingProfile}>
              <Save className="size-4" />
              Guardar cambios
            </Button>
          </form>
        </Card.Content>
      </Card>

      <Card className="w-full">
        <Card.Header>
          <Card.Title className="flex items-center gap-2 text-lg">
            <KeyRound className="size-5" />
            Cambiar Contraseña
          </Card.Title>
          <Card.Description>Actualice su contraseña de acceso</Card.Description>
        </Card.Header>
        <Card.Content>
          <form onSubmit={handlePasswordSubmit} className="space-y-4">
            <TextField isRequired className="space-y-1">
              <Label className="text-sm font-medium">Contraseña actual</Label>
              <InputGroup>
                <InputGroup.Input
                  id="currentPassword"
                  type={showCurrentPassword ? "text" : "password"}
                  value={passwordData.currentPassword}
                  onChange={handlePasswordChange}
                />
                <InputGroup.Suffix className="pr-0">
                  <Button
                    isIconOnly
                    aria-label={showCurrentPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                    size="sm"
                    variant="ghost"
                    onPress={() => setShowCurrentPassword(!showCurrentPassword)}
                  >
                    {showCurrentPassword ? (
                      <EyeOff className="size-4" />
                    ) : (
                      <Eye className="size-4" />
                    )}
                  </Button>
                </InputGroup.Suffix>
              </InputGroup>
            </TextField>
            <TextField isRequired className="space-y-1">
              <Label className="text-sm font-medium">Nueva contraseña</Label>
              <InputGroup>
                <InputGroup.Input
                  id="newPassword"
                  type={showNewPassword ? "text" : "password"}
                  value={passwordData.newPassword}
                  onChange={handlePasswordChange}
                />
                <InputGroup.Suffix className="pr-0">
                  <Button
                    isIconOnly
                    aria-label={showNewPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                    size="sm"
                    variant="ghost"
                    onPress={() => setShowNewPassword(!showNewPassword)}
                  >
                    {showNewPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                  </Button>
                </InputGroup.Suffix>
              </InputGroup>
              <p className="text-xs text-muted">
                Mínimo 8 caracteres, con mayúscula, minúscula, dígito y carácter especial
                (@#$%^&+=!)
              </p>
            </TextField>
            <Button type="submit" variant="secondary" isPending={isSavingPassword}>
              <KeyRound className="size-4" />
              Actualizar contraseña
            </Button>
          </form>
        </Card.Content>
      </Card>
    </div>
  );
}
