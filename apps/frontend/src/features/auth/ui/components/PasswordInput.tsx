import { useState, useCallback } from "react";
import { Button, InputGroup, Label, TextField } from "@heroui/react";
import { Eye, EyeOff } from "lucide-react";

interface PasswordInputProps {
  id: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  label?: string;
  placeholder?: string;
  error?: string | null;
  className?: string;
  required?: boolean;
}

export function PasswordInput({
  id,
  value,
  onChange,
  label = "Contraseña",
  placeholder = "••••••••",
  error,
  className,
  required = true,
}: PasswordInputProps) {
  const [showPassword, setShowPassword] = useState(false);

  const toggleShowPassword = useCallback(() => {
    setShowPassword((prev) => !prev);
  }, []);

  return (
    <TextField isRequired={required} isInvalid={!!error} className={className}>
      {label && <Label className="text-sm font-medium text-black">{label}</Label>}
      <InputGroup>
        <InputGroup.Input
          id={id}
          type={showPassword ? "text" : "password"}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          required={required}
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
      {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
    </TextField>
  );
}
