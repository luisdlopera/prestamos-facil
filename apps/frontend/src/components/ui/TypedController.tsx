"use client";

import type { ReactElement } from "react";
import { Controller } from "react-hook-form";
import type { Control, FieldValues, Path } from "react-hook-form";

interface TypedControllerProps<T extends FieldValues> {
  name: Path<T>;
  control: Control<T>;
  render: (props: {
    value: unknown;
    onChange: (...event: unknown[]) => void;
    error?: string;
  }) => ReactElement;
}

export function TypedController<T extends FieldValues>({
  name,
  control,
  render,
}: TypedControllerProps<T>) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field, fieldState }) =>
        render({
          value: field.value,
          onChange: field.onChange,
          error: fieldState.error?.message,
        })
      }
    />
  );
}
