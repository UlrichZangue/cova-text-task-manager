const shortDate = new Intl.DateTimeFormat("fr-FR", {
  day: "2-digit",
  month: "short",
  year: "numeric",
});

export function formatDate(value?: string | null): string {
  if (!value) return "Sans echeance";
  return shortDate.format(new Date(`${value}T00:00:00`));
}

export function isOverdue(value?: string | null): boolean {
  if (!value) return false;
  const due = new Date(`${value}T23:59:59`);
  return due.getTime() < Date.now();
}
