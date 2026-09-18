import type { TaskRepository } from "../domain/task.repository";
import type { TaskFilters, TaskInput } from "../domain/task.types";

export class GetTasksUseCase {
  constructor(private readonly repository: TaskRepository) {}
  execute(filters: TaskFilters) {
    return this.repository.findAll(filters);
  }
}
export class GetTaskStatsUseCase {
  constructor(private readonly repository: TaskRepository) {}
  execute() {
    return this.repository.getStats();
  }
}
export class CreateTaskUseCase {
  constructor(private readonly repository: TaskRepository) {}
  execute(input: TaskInput) {
    return this.repository.create(input);
  }
}
export class UpdateTaskUseCase {
  constructor(private readonly repository: TaskRepository) {}
  execute(id: string, input: TaskInput) {
    return this.repository.update(id, input);
  }
}
export class DeleteTaskUseCase {
  constructor(private readonly repository: TaskRepository) {}
  execute(id: string) {
    return this.repository.delete(id);
  }
}
