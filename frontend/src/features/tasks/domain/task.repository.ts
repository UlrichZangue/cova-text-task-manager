import type { PageResponse, Task, TaskFilters, TaskInput, TaskStats } from "./task.types";

export interface TaskRepository {
  findAll(filters: TaskFilters): Promise<PageResponse<Task>>;
  getStats(): Promise<TaskStats>;
  create(input: TaskInput): Promise<Task>;
  update(id: string, input: TaskInput): Promise<Task>;
  delete(id: string): Promise<void>;
}
