"use client";

import { createContext, useCallback, useContext, useState } from "react";
import { Button, Modal } from "@heroui/react";

interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: "primary" | "danger";
}

interface PromptOptions {
  title: string;
  message: string;
  inputLabel?: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

interface DialogState {
  type: "confirm" | "prompt" | null;
  isOpen: boolean;
  options: ConfirmOptions | PromptOptions | null;
  resolve: ((value: boolean | string | null) => void) | null;
}

interface DialogContextValue {
  confirm: (options: ConfirmOptions) => Promise<boolean>;
  prompt: (options: PromptOptions) => Promise<string | null>;
}

const DialogContext = createContext<DialogContextValue | null>(null);

export function useDialog(): DialogContextValue {
  const ctx = useContext(DialogContext);
  if (!ctx) throw new Error("useDialog must be used within DialogProvider");
  return ctx;
}

export function DialogProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<DialogState>({
    type: null,
    isOpen: false,
    options: null,
    resolve: null,
  });

  const confirm = useCallback((options: ConfirmOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      setState({
        type: "confirm",
        isOpen: true,
        options,
        resolve: resolve as (value: boolean | string | null) => void,
      });
    });
  }, []);

  const prompt = useCallback((options: PromptOptions): Promise<string | null> => {
    return new Promise((resolve) => {
      setState({
        type: "prompt",
        isOpen: true,
        options,
        resolve: resolve as (value: boolean | string | null) => void,
      });
    });
  }, []);

  const handleClose = useCallback(() => {
    state.resolve?.(false);
    setState({ type: null, isOpen: false, options: null, resolve: null });
  }, [state.resolve]);

  const handleConfirm = useCallback(() => {
    state.resolve?.(true);
    setState({ type: null, isOpen: false, options: null, resolve: null });
  }, [state.resolve]);

  return (
    <DialogContext.Provider value={{ confirm, prompt }}>
      {children}

      {state.type === "confirm" && state.options && (
        <Modal>
          <Modal.Backdrop
            isOpen={state.isOpen}
            onOpenChange={(open) => {
              if (!open) handleClose();
            }}
          >
            <Modal.Container>
              <Modal.Dialog className="sm:max-w-[400px]">
                <Modal.CloseTrigger />
                <Modal.Header>
                  <Modal.Heading>{(state.options as ConfirmOptions).title}</Modal.Heading>
                </Modal.Header>
                <Modal.Body>
                  <p className="text-sm text-muted">{(state.options as ConfirmOptions).message}</p>
                </Modal.Body>
                <Modal.Footer>
                  <Button slot="close" variant="tertiary">
                    {(state.options as ConfirmOptions).cancelLabel ?? "Cancelar"}
                  </Button>
                  <Button
                    variant={
                      (state.options as ConfirmOptions).variant === "danger" ? "danger" : "primary"
                    }
                    onPress={handleConfirm}
                  >
                    {(state.options as ConfirmOptions).confirmLabel ?? "Confirmar"}
                  </Button>
                </Modal.Footer>
              </Modal.Dialog>
            </Modal.Container>
          </Modal.Backdrop>
        </Modal>
      )}
    </DialogContext.Provider>
  );
}
