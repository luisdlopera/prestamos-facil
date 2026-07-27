export const a11y = {
  button: {
    delete: (entity: string) => ({
      "aria-label": `Eliminar ${entity}`,
    }),
    edit: (entity: string) => ({
      "aria-label": `Editar ${entity}`,
    }),
    view: (entity: string) => ({
      "aria-label": `Ver detalle de ${entity}`,
    }),
  },
  modal: {
    close: () => ({
      "aria-label": "Cerrar",
    }),
  },
  table: {
    selectRow: (id: string) => ({
      "aria-label": `Seleccionar fila ${id}`,
    }),
    sortBy: (column: string) => ({
      "aria-label": `Ordenar por ${column}`,
    }),
  },
};

export function announce(message: string): void {
  const id = "a11y-announce";
  let el = document.getElementById(id) as HTMLDivElement | null;
  if (!el) {
    el = document.createElement("div");
    el.id = id;
    el.setAttribute("role", "status");
    el.setAttribute("aria-live", "polite");
    el.className = "sr-only";
    document.body.appendChild(el);
  }
  el.textContent = "";
  requestAnimationFrame(() => {
    el!.textContent = message;
  });
}
