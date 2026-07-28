"use client";

import { useState, useCallback, useRef } from "react";
import { Button, Card, Input, InputGroup, Label, TextField } from "@heroui/react";
import { Eye, EyeOff } from "lucide-react";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useAsyncAction } from "@/lib/errors";
import { loginUnified } from "../infrastructure/auth-service";

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const setUser = useAuthStore((s) => s.setUser);
  const redirectUrlRef = useRef("/my-loans");
  const registered =
    typeof window !== "undefined" &&
    new URLSearchParams(window.location.search).get("registered") === "1";

  const { run, isLoading } = useAsyncAction({
    successMsg: "Inicio de sesión exitoso",
    onSuccess: () => {
      window.location.href = redirectUrlRef.current;
    },
  });

  const handleSubmit = useCallback(
    (e: React.SyntheticEvent<HTMLFormElement>) => {
      e.preventDefault();
      run(async () => {
        const result = await loginUnified({ email, password });
        setUser(result.user, result.userType);
        redirectUrlRef.current = result.redirectUrl;
      });
    },
    [email, password, run, setUser],
  );

  const toggleShowPassword = useCallback(() => {
    setShowPassword((prev) => !prev);
  }, []);

  return (
    <Card className="w-full max-w-sm border-0 shadow-lg">
      <Card.Header>
        <Card.Title className="text-xl">Iniciar Sesión</Card.Title>
        <Card.Description>Ingrese sus credenciales para acceder al sistema</Card.Description>
        {registered && (
          <p className="text-sm text-green-700">Registro exitoso. Ahora inicie sesión.</p>
        )}
      </Card.Header>
      <Card.Content>
        <form onSubmit={handleSubmit} className="space-y-4">
          <TextField isRequired className="space-y-1">
            <Label className="text-sm font-medium text-black">Correo electrónico</Label>
            <Input
              id="email"
              type="email"
              placeholder="Ingrese su correo electrónico"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </TextField>
          <TextField isRequired className="space-y-1">
            <Label className="text-sm font-medium text-black">Contraseña</Label>
            <InputGroup>
              <InputGroup.Input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
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
          </TextField>
          <Button type="submit" fullWidth isPending={isLoading}>
            Iniciar Sesión
          </Button>
        </form>
        <div className="mt-4 flex justify-between text-sm">
          <a href="/auth/forgot-password" className="text-blue-600 hover:underline">
            ¿Olvidó su contraseña?
          </a>
          <a href="/auth/register" className="text-blue-600 hover:underline">
            Registrarse
          </a>
        </div>
      </Card.Content>
    </Card>
  );
}
