import { HttpTaskRepository } from "../infrastructure/http-task.repository";
import {
  CreateTaskUseCase,
  DeleteTaskUseCase,
  GetTasksUseCase,
  GetTaskStatsUseCase,
  UpdateTaskUseCase,
} from "./task.use-cases";

const repository = new HttpTaskRepository();
export const getTasksUseCase = new GetTasksUseCase(repository);
export const getTaskStatsUseCase = new GetTaskStatsUseCase(repository);
export const createTaskUseCase = new CreateTaskUseCase(repository);
export const updateTaskUseCase = new UpdateTaskUseCase(repository);
export const deleteTaskUseCase = new DeleteTaskUseCase(repository);
