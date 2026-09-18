import type { TaskPriority, TaskStatus } from "../domain/task.types";

export const statusLabels: Record<TaskStatus, string> = {
  TODO: "A faire",
  IN_PROGRESS: "En cours",
  DONE: "Terminee",
};

export const priorityLabels: Record<TaskPriority, string> = {
  LOW: "Basse",
  MEDIUM: "Moyenne",
  HIGH: "Haute",
};
