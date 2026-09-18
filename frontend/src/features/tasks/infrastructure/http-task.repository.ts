import { apiClient } from "../../../core/http/api-client";
import { endpoints } from "../../../core/http/endpoints";
import type { TaskRepository } from "../domain/task.repository";
import type { PageResponse, Task, TaskFilters, TaskInput, TaskStats } from "../domain/task.types";

export class HttpTaskRepository implements TaskRepository {
  findAll(filters: TaskFilters): Promise<PageResponse<Task>> {
    const params = new URLSearchParams({
      page: String(filters.page),
      size: String(filters.size),
      sort: filters.sort,
    });
    if (filters.search.trim()) params.set("search", filters.search.trim());
    if (filters.status !== "ALL") params.set("status", filters.status);
    if (filters.priority !== "ALL") params.set("priority", filters.priority);
    return apiClient.get<PageResponse<Task>>(`${endpoints.tasks.all}?${params}`);
  }

  getStats(): Promise<TaskStats> {
    return apiClient.get<TaskStats>(endpoints.tasks.stats);
  }

  create(input: TaskInput): Promise<Task> {
    return apiClient.post<Task>(endpoints.tasks.all, input);
  }

  update(id: string, input: TaskInput): Promise<Task> {
    return apiClient.put<Task>(endpoints.tasks.byId(id), input);
  }

  delete(id: string): Promise<void> {
    return apiClient.delete(endpoints.tasks.byId(id));
  }
}
