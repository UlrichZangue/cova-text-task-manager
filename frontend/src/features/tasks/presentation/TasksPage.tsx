import { ChevronLeft, ChevronRight, Plus, SearchX, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "../../../shared/components/Button";
import { Modal } from "../../../shared/components/Modal";
import { StateView } from "../../../shared/components/StateView";
import type {
  Task,
  TaskFilters as TaskFiltersValue,
  TaskPriority,
  TaskStatus,
} from "../domain/task.types";
import { TaskFilters } from "./TaskFilters";
import { TaskForm } from "./TaskForm";
import { TaskKanban } from "./TaskKanban";
import { useDeleteTask, useTasks, useUpdateTask } from "./task.queries";

const initialFilters: TaskFiltersValue = {
  search: "",
  status: "ALL",
  priority: "ALL",
  page: 0,
  size: 30,
  sort: "createdAt,desc",
};

export function TasksPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState(initialFilters);
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [deletingTask, setDeletingTask] = useState<Task | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const deleteTask = useDeleteTask();
  const updateTask = useUpdateTask();

  useEffect(() => {
    const timeout = window.setTimeout(() => setDebouncedSearch(filters.search), 350);
    return () => window.clearTimeout(timeout);
  }, [filters.search]);

  useEffect(() => {
    if (searchParams.get("create") === "true") {
      setEditingTask(null);
      setFormOpen(true);
      setSearchParams({}, { replace: true });
    }
  }, [searchParams, setSearchParams]);

  const queryFilters = { ...filters, search: debouncedSearch };
  const tasks = useTasks(queryFilters);
  const updateFilter = <K extends keyof TaskFiltersValue>(key: K, value: TaskFiltersValue[K]) =>
    setFilters((current) => ({
      ...current,
      [key]: value,
      page: key === "page" ? (value as number) : 0,
    }));

  const openCreate = () => {
    setEditingTask(null);
    setFormOpen(true);
  };
  const openEdit = (task: Task) => {
    setEditingTask(task);
    setFormOpen(true);
  };
  const closeForm = () => {
    setFormOpen(false);
    setEditingTask(null);
  };

  const confirmDelete = async () => {
    if (!deletingTask) return;
    try {
      await deleteTask.mutateAsync(deletingTask.id);
      toast.success("Tache supprimee");
      setDeletingTask(null);
    } catch {
      toast.error("La suppression a echoue.");
    }
  };

  const moveTask = async (task: Task, status: TaskStatus) => {
    try {
      await updateTask.mutateAsync({
        id: task.id,
        input: {
          title: task.title,
          description: task.description ?? "",
          priority: task.priority,
          dueDate: task.dueDate,
          status,
        },
      });
      toast.success("Statut de la tache mis a jour");
      return true;
    } catch {
      toast.error("Le deplacement de la tache a echoue.");
      return false;
    }
  };

  return (
    <div className="page-content">
      <header className="page-header page-header--actions">
        <div>
          <p className="eyebrow">Organisation</p>
          <h1>Mes taches</h1>
          <p>
            {tasks.data
              ? `${tasks.data.totalElements} tache${tasks.data.totalElements > 1 ? "s" : ""} dans votre espace`
              : "Pilotez votre travail au meme endroit."}
          </p>
        </div>
        <Button onClick={openCreate}>
          <Plus /> Nouvelle tache
        </Button>
      </header>
      <TaskFilters
        search={filters.search}
        priority={filters.priority}
        onSearchChange={(value) => updateFilter("search", value)}
        onPriorityChange={(value: TaskPriority | "ALL") => updateFilter("priority", value)}
      />

      {tasks.isLoading ? (
        <StateView type="loading" message="Chargement de vos taches..." />
      ) : tasks.isError ? (
        <StateView
          type="error"
          message="Impossible de charger les taches."
          actionLabel="Reessayer"
          onAction={() => tasks.refetch()}
        />
      ) : tasks.data!.content.length === 0 ? (
        <StateView
          type="empty"
          icon={SearchX}
          title={
            filters.search || filters.status !== "ALL" || filters.priority !== "ALL"
              ? "Aucun resultat"
              : "Votre liste est vide"
          }
          message={
            filters.search || filters.status !== "ALL" || filters.priority !== "ALL"
              ? "Essayez de modifier vos criteres."
              : "Creez votre premiere tache pour commencer."
          }
          actionLabel={
            filters.search || filters.status !== "ALL" || filters.priority !== "ALL"
              ? "Reinitialiser"
              : "Creer une tache"
          }
          onAction={() =>
            filters.search || filters.status !== "ALL" || filters.priority !== "ALL"
              ? setFilters(initialFilters)
              : openCreate()
          }
        />
      ) : (
        <>
          <TaskKanban
            tasks={tasks.data!.content}
            moving={updateTask.isPending}
            onEdit={openEdit}
            onDelete={setDeletingTask}
            onMove={moveTask}
          />
          <nav className="pagination" aria-label="Pagination">
            <p>
              Page <strong>{tasks.data!.page + 1}</strong> sur{" "}
              <strong>{Math.max(tasks.data!.totalPages, 1)}</strong>
            </p>
            <div>
              <Button
                variant="secondary"
                size="icon"
                disabled={tasks.data!.first}
                onClick={() => updateFilter("page", filters.page - 1)}
                aria-label="Page precedente"
              >
                <ChevronLeft />
              </Button>
              <Button
                variant="secondary"
                size="icon"
                disabled={tasks.data!.last}
                onClick={() => updateFilter("page", filters.page + 1)}
                aria-label="Page suivante"
              >
                <ChevronRight />
              </Button>
            </div>
          </nav>
        </>
      )}

      <Modal
        open={formOpen}
        title={editingTask ? "Modifier la tache" : "Nouvelle tache"}
        description={
          editingTask
            ? "Mettez a jour les informations utiles."
            : "Transformez une intention en prochaine action."
        }
        onClose={closeForm}
      >
        <TaskForm task={editingTask} onCancel={closeForm} onSuccess={closeForm} />
      </Modal>
      <Modal
        open={Boolean(deletingTask)}
        title="Supprimer cette tache ?"
        description="Cette action retirera la tache de votre espace."
        onClose={() => setDeletingTask(null)}
      >
        <div className="confirm-dialog">
          <div className="confirm-dialog__icon">
            <Trash2 />
          </div>
          <p>
            <strong>{deletingTask?.title}</strong> ne sera plus visible dans votre liste.
          </p>
          <div className="modal__actions">
            <Button variant="secondary" onClick={() => setDeletingTask(null)}>
              Annuler
            </Button>
            <Button variant="danger" loading={deleteTask.isPending} onClick={confirmDelete}>
              Supprimer
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
