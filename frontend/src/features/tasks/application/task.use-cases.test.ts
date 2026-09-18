import { describe, expect, it, vi } from "vitest";
import type { TaskRepository } from "../domain/task.repository";
import type { Task } from "../domain/task.types";
import { CreateTaskUseCase, DeleteTaskUseCase, GetTasksUseCase } from "./task.use-cases";

const task: Task = {
  id: "task-1",
  title: "Preparer le rapport",
  description: null,
  status: "TODO",
  priority: "HIGH",
  dueDate: null,
  createdAt: "2026-09-18T10:00:00",
  updatedAt: "2026-09-18T10:00:00",
};

function repositoryMock(): TaskRepository {
  return {
    findAll: vi.fn().mockResolvedValue({
      content: [task],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    }),
    getStats: vi.fn(),
    create: vi.fn().mockResolvedValue(task),
    update: vi.fn(),
    delete: vi.fn().mockResolvedValue(undefined),
  };
}

describe("task use cases", () => {
  it("delegates filters to the repository", async () => {
    const repository = repositoryMock();
    const filters = {
      search: "rapport",
      status: "TODO" as const,
      priority: "ALL" as const,
      page: 0,
      size: 10,
      sort: "createdAt,desc",
    };
    const result = await new GetTasksUseCase(repository).execute(filters);
    expect(repository.findAll).toHaveBeenCalledWith(filters);
    expect(result.content).toEqual([task]);
  });

  it("uses the backend response when creating a task", async () => {
    const repository = repositoryMock();
    const input = { title: "Preparer le rapport", priority: "HIGH" as const };
    await expect(new CreateTaskUseCase(repository).execute(input)).resolves.toEqual(task);
    expect(repository.create).toHaveBeenCalledWith(input);
  });

  it("deletes the requested task", async () => {
    const repository = repositoryMock();
    await new DeleteTaskUseCase(repository).execute("task-1");
    expect(repository.delete).toHaveBeenCalledWith("task-1");
  });
});
