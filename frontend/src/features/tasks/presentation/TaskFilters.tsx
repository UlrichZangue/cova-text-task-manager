import { Search, SlidersHorizontal } from "lucide-react";
import type { TaskPriority } from "../domain/task.types";

interface TaskFiltersProps {
  search: string;
  priority: TaskPriority | "ALL";
  onSearchChange: (value: string) => void;
  onPriorityChange: (value: TaskPriority | "ALL") => void;
}

export function TaskFilters({
  search,
  priority,
  onSearchChange,
  onPriorityChange,
}: TaskFiltersProps) {
  return (
    <div className="filter-bar">
      <label className="search-field">
        <Search aria-hidden="true" />
        <span className="sr-only">Rechercher</span>
        <input
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Rechercher une tache..."
        />
      </label>
      <div className="filter-select">
        <SlidersHorizontal aria-hidden="true" />
        <select
          aria-label="Filtrer par priorite"
          value={priority}
          onChange={(event) => onPriorityChange(event.target.value as TaskPriority | "ALL")}
        >
          <option value="ALL">Toutes les priorites</option>
          <option value="HIGH">Haute</option>
          <option value="MEDIUM">Moyenne</option>
          <option value="LOW">Basse</option>
        </select>
      </div>
    </div>
  );
}
