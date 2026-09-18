import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { ApiError } from "../../../core/http/api-error";
import { Button } from "../../../shared/components/Button";
import { InputField, TextareaField } from "../../../shared/components/FormField";
import type { Task, TaskInput } from "../domain/task.types";
import { priorityLabels, statusLabels } from "./task.constants";
import { taskSchema, type TaskFormData } from "./task.schemas";
import { useCreateTask, useUpdateTask } from "./task.queries";

interface TaskFormProps {
  task?: Task | null;
  onSuccess: () => void;
  onCancel: () => void;
}

const defaults: TaskFormData = {
  title: "",
  description: "",
  status: "TODO",
  priority: "MEDIUM",
  dueDate: "",
};

export function TaskForm({ task, onSuccess, onCancel }: TaskFormProps) {
  const createTask = useCreateTask();
  const updateTask = useUpdateTask();
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<TaskFormData>({ resolver: zodResolver(taskSchema), defaultValues: defaults });

  useEffect(() => {
    reset(
      task
        ? {
            title: task.title,
            description: task.description ?? "",
            status: task.status,
            priority: task.priority,
            dueDate: task.dueDate ?? "",
          }
        : defaults,
    );
  }, [task, reset]);

  const onSubmit = handleSubmit(async (values) => {
    const input: TaskInput = { ...values, dueDate: values.dueDate || null };
    try {
      if (task) await updateTask.mutateAsync({ id: task.id, input });
      else await createTask.mutateAsync(input);
      toast.success(task ? "Tache modifiee" : "Tache creee");
      onSuccess();
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fieldErrors).forEach(([field, message]) =>
          setError(field as keyof TaskFormData, { message }),
        );
        toast.error(error.message);
      } else toast.error("La tache n'a pas pu etre enregistree.");
    }
  });

  const pending = createTask.isPending || updateTask.isPending;
  return (
    <form onSubmit={onSubmit} className="task-form" noValidate>
      <InputField
        label="Titre"
        placeholder="Ex. Preparer la reunion client"
        autoFocus
        error={errors.title?.message}
        {...register("title")}
      />
      <TextareaField
        label="Description"
        placeholder="Ajoutez le contexte utile..."
        rows={4}
        error={errors.description?.message}
        {...register("description")}
      />
      <div className="form-grid form-grid--three">
        <div className="field">
          <label className="field__label" htmlFor="task-status">
            Statut
          </label>
          <select id="task-status" className="field__control" {...register("status")}>
            {Object.entries(statusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label className="field__label" htmlFor="task-priority">
            Priorite
          </label>
          <select id="task-priority" className="field__control" {...register("priority")}>
            {Object.entries(priorityLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <InputField
          label="Echeance"
          type="date"
          error={errors.dueDate?.message}
          {...register("dueDate")}
        />
      </div>
      <div className="modal__actions">
        <Button type="button" variant="secondary" onClick={onCancel}>
          Annuler
        </Button>
        <Button type="submit" loading={pending}>
          {task ? "Enregistrer" : "Creer la tache"}
        </Button>
      </div>
    </form>
  );
}
