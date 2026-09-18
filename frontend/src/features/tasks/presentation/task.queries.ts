import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createTaskUseCase,
  deleteTaskUseCase,
  getTasksUseCase,
  getTaskStatsUseCase,
  updateTaskUseCase,
} from "../application/task.dependencies";
import type { TaskFilters, TaskInput } from "../domain/task.types";

export const taskKeys = {
  all: ["tasks"] as const,
  lists: () => [...taskKeys.all, "list"] as const,
  list: (filters: TaskFilters) => [...taskKeys.lists(), filters] as const,
  stats: () => [...taskKeys.all, "stats"] as const,
};

export function useTasks(filters: TaskFilters) {
  return useQuery({
    queryKey: taskKeys.list(filters),
    queryFn: () => getTasksUseCase.execute(filters),
    placeholderData: keepPreviousData,
  });
}

export function useTaskStats() {
  return useQuery({ queryKey: taskKeys.stats(), queryFn: () => getTaskStatsUseCase.execute() });
}

function useRefreshTasks() {
  const client = useQueryClient();
  return () =>
    Promise.all([
      client.invalidateQueries({ queryKey: taskKeys.lists() }),
      client.invalidateQueries({ queryKey: taskKeys.stats() }),
    ]);
}

export function useCreateTask() {
  const refresh = useRefreshTasks();
  return useMutation({
    mutationFn: (input: TaskInput) => createTaskUseCase.execute(input),
    onSuccess: refresh,
  });
}

export function useUpdateTask() {
  const refresh = useRefreshTasks();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: TaskInput }) =>
      updateTaskUseCase.execute(id, input),
    onSuccess: refresh,
  });
}

export function useDeleteTask() {
  const refresh = useRefreshTasks();
  return useMutation({
    mutationFn: (id: string) => deleteTaskUseCase.execute(id),
    onSuccess: refresh,
  });
}
