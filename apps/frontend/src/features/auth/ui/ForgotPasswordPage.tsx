"use client";

import { useState, useEffect } from "react";
import { Button, Card, Input, Label, TextField } from "@heroui/react";
import { COLD_START_TIMEOUT, post } from "@/lib/api/client";
import { useAsyncAction } from "@/lib/errors";
import { useSlowRequestHint } from "@/lib/hooks/useSlowRequestHint";
import { PasswordInput } from "./components/PasswordInput";

function validatePassword(password: string): string | null {
  if (password.length < 8) return "La contraseña debe tener al menos 8 caracteres";
  if (!/[A-Z]/.test(password)) return "Debe incluir una mayúscula";
  if (!/[0-9]/.test(password)) return "Debe incluir un número";
  if (!/[!@#$%^&+=]/.test(password)) return "Debe incluir un carácter especial (!@#$%^&+=)";
  return null;
}

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [step, setStep] = useState<"request" | "confirm" | "sent">("request");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token") || "";
    if (token) {
      setResetToken(token);
      setStep("confirm");
    }
  }, []);

  const { run: runRequest, isLoading: isLoadingRequest } = useAsyncAction({
    successMsg: "Si el correo existe, recibirá un enlace de recuperación",
    onSuccess: () => {
      setStep("sent");
    },
  });

  const { run: runConfirm, isLoading: isLoadingConfirm } = useAsyncAction({
    successMsg: "Contraseña restablecida exitosamente",
    onSuccess: () => {
      setTimeout(() => {
        window.location.href = "/login";
      }, 2000);
    },
  });

  const isSlowRequest = useSlowRequestHint(isLoadingRequest);
  const isSlowConfirm = useSlowRequestHint(isLoadingConfirm);

  const handleRequestReset = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    runRequest(async () => {
      await post("/auth/password-reset/request", { email }, { timeout: COLD_START_TIMEOUT });
    });
  };

  const handleConfirmReset = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setPasswordError(null);
    const error = validatePassword(newPassword);
    if (error) {
      setPasswordError(error);
      return;
    }
    runConfirm(async () => {
      await post(
        "/auth/password-reset/confirm",
        { token: resetToken, newPassword },
        { timeout: COLD_START_TIMEOUT },
      );
    });
  };

  return (
    <Card className="w-full max-w-sm border-0 shadow-lg">
      <Card.Header>
        <Card.Title className="text-xl">Recuperar Contraseña</Card.Title>
        <Card.Description>
          {step === "request" && "Ingrese su correo electrónico"}
          {step === "sent" && "Revise su bandeja de entrada"}
          {step === "confirm" && "Ingrese su nueva contraseña"}
        </Card.Description>
      </Card.Header>
      <Card.Content>
        {step === "request" && (
          <form onSubmit={handleRequestReset} className="space-y-4">
            <TextField isRequired>
              <Label className="text-sm font-medium text-black">Correo electrónico</Label>
              <Input
                id="email"
                type="email"
                placeholder="Ingrese su correo electrónico"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </TextField>
            <Button type="submit" fullWidth isPending={isLoadingRequest}>
              Enviar enlace de recuperación
            </Button>
            {isSlowRequest && (
              <p className="text-xs text-gray-500 text-center">
                Esto puede tardar unos segundos si el servidor estaba inactivo…
              </p>
            )}
          </form>
        )}
        {step === "sent" && (
          <p className="text-sm text-gray-600">
            Si el correo existe en nuestro sistema, recibirá un enlace de recuperación en los
            próximos minutos. Haga clic en el enlace del correo para continuar y definir su nueva
            contraseña.
          </p>
        )}
        {step === "confirm" && (
          <form onSubmit={handleConfirmReset} className="space-y-4">
            <PasswordInput
              id="newPassword"
              label="Nueva contraseña"
              value={newPassword}
              onChange={(e) => {
                setNewPassword(e.target.value);
                setPasswordError(null);
              }}
              error={passwordError}
              required
            />
            <Button type="submit" fullWidth isPending={isLoadingConfirm}>
              Confirmar nueva contraseña
            </Button>
            {isSlowConfirm && (
              <p className="text-xs text-gray-500 text-center">
                Esto puede tardar unos segundos si el servidor estaba inactivo…
              </p>
            )}
          </form>
        )}
        <div className="mt-4 text-center">
          <a href="/login" className="text-blue-600 hover:underline text-sm">
            Volver a iniciar sesión
          </a>
        </div>
      </Card.Content>
    </Card>
  );
}
