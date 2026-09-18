import type { LucideIcon } from "lucide-react";
import { CircleAlert, ClipboardList, LoaderCircle } from "lucide-react";
import { Button } from "./Button";

interface StateViewProps {
  type: "loading" | "empty" | "error";
  title?: string;
  message?: string;
  actionLabel?: string;
  onAction?: () => void;
  icon?: LucideIcon;
}

export function StateView({
  type,
  title,
  message,
  actionLabel,
  onAction,
  icon: CustomIcon,
}: StateViewProps) {
  const Icon =
    CustomIcon ??
    (type === "error" ? CircleAlert : type === "empty" ? ClipboardList : LoaderCircle);
  const defaultTitle =
    type === "loading"
      ? "Chargement"
      : type === "empty"
        ? "Aucune donnee"
        : "Une erreur est survenue";

  return (
    <div className="state-view" role={type === "error" ? "alert" : "status"}>
      <Icon className={type === "loading" ? "state-view__spinner" : ""} aria-hidden="true" />
      <h3>{title ?? defaultTitle}</h3>
      {message && <p>{message}</p>}
      {actionLabel && onAction && <Button onClick={onAction}>{actionLabel}</Button>}
    </div>
  );
}
