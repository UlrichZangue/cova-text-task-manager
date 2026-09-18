import { CalendarDays, GripVertical, MoreHorizontal, Pencil, Trash2 } from "lucide-react";
import type { ButtonHTMLAttributes, Ref } from "react";
import { useState } from "react";
import { Button } from "../../../shared/components/Button";
import { cn } from "../../../shared/lib/cn";
import { formatDate, isOverdue } from "../../../shared/lib/date";
import type { Task } from "../domain/task.types";
import { priorityLabels, statusLabels } from "./task.constants";

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
  dragging?: boolean;
  dragHandleProps?: ButtonHTMLAttributes<HTMLButtonElement> & {
    ref?: Ref<HTMLButtonElement>;
  };
}

export function TaskCard({
  task,
  onEdit,
  onDelete,
  dragging = false,
  dragHandleProps,
}: TaskCardProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  return (
    <article className={cn("task-card", dragging && "task-card--dragging")}>
      <div className="task-card__top">
        <div className="task-card__state">
          {dragHandleProps ? (
            <button
              className="task-card__drag-handle"
              type="button"
              aria-label={`Deplacer ${task.title}`}
              {...dragHandleProps}
            >
              <GripVertical aria-hidden="true" />
            </button>
          ) : (
            <GripVertical className="task-card__grip" aria-hidden="true" />
          )}
          <span className={cn("status-badge", `status-badge--${task.status.toLowerCase()}`)}>
            {statusLabels[task.status]}
          </span>
        </div>
        <div className="task-menu">
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Actions pour ${task.title}`}
            onClick={() => setMenuOpen((value) => !value)}
          >
            <MoreHorizontal />
          </Button>
          {menuOpen && (
            <div className="task-menu__popover">
              <button
                onClick={() => {
                  setMenuOpen(false);
                  onEdit(task);
                }}
              >
                <Pencil /> Modifier
              </button>
              <button
                className="danger-text"
                onClick={() => {
                  setMenuOpen(false);
                  onDelete(task);
                }}
              >
                <Trash2 /> Supprimer
              </button>
            </div>
          )}
        </div>
      </div>
      <div className="task-card__body">
        <h3>{task.title}</h3>
        <p>{task.description || "Aucune description"}</p>
      </div>
      <footer className="task-card__footer">
        <span className={cn("priority-dot", `priority-dot--${task.priority.toLowerCase()}`)}>
          <i />
          {priorityLabels[task.priority]}
        </span>
        <span
          className={cn(
            "due-date",
            task.status !== "DONE" && isOverdue(task.dueDate) && "due-date--late",
          )}
        >
          <CalendarDays />
          {formatDate(task.dueDate)}
        </span>
      </footer>
    </article>
  );
}
