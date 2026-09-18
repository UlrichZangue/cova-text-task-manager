import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  useDraggable,
  useDroppable,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from "@dnd-kit/core";
import { CheckCircle2, CircleDot, Clock3 } from "lucide-react";
import { useEffect, useState } from "react";
import { cn } from "../../../shared/lib/cn";
import type { Task, TaskStatus } from "../domain/task.types";
import { TaskCard } from "./TaskCard";

interface TaskKanbanProps {
  tasks: Task[];
  moving: boolean;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
  onMove: (task: Task, status: TaskStatus) => Promise<boolean>;
}

const columns = [
  { status: "TODO", label: "A faire", icon: CircleDot, tone: "todo" },
  { status: "IN_PROGRESS", label: "En cours", icon: Clock3, tone: "in_progress" },
  { status: "DONE", label: "Terminees", icon: CheckCircle2, tone: "done" },
] satisfies Array<{ status: TaskStatus; label: string; icon: typeof CircleDot; tone: string }>;

interface KanbanCardProps {
  task: Task;
  active: boolean;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
}

function KanbanCard({ task, active, onEdit, onDelete }: KanbanCardProps) {
  const { attributes, listeners, setNodeRef, setActivatorNodeRef, transform } = useDraggable({
    id: task.id,
    data: { task },
  });
  const style = transform
    ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)` }
    : undefined;

  return (
    <div ref={setNodeRef} className="kanban-card-shell" style={style} {...listeners}>
      <TaskCard
        task={task}
        onEdit={onEdit}
        onDelete={onDelete}
        dragging={active}
        dragHandleProps={{ ...attributes, ...listeners, ref: setActivatorNodeRef }}
      />
    </div>
  );
}

interface KanbanColumnProps extends Omit<TaskKanbanProps, "tasks" | "moving" | "onMove"> {
  status: TaskStatus;
  label: string;
  tone: string;
  icon: typeof CircleDot;
  tasks: Task[];
  activeId: string | null;
}

function KanbanColumn({
  status,
  label,
  tone,
  icon: Icon,
  tasks,
  activeId,
  onEdit,
  onDelete,
}: KanbanColumnProps) {
  const { isOver, setNodeRef } = useDroppable({ id: status });

  return (
    <section
      ref={setNodeRef}
      className={cn("kanban-column", `kanban-column--${tone}`, isOver && "kanban-column--target")}
      aria-labelledby={`kanban-${status}`}
    >
      <header className="kanban-column__header">
        <div>
          <span className="kanban-column__icon">
            <Icon />
          </span>
          <h2 id={`kanban-${status}`}>{label}</h2>
        </div>
        <span className="kanban-column__count" aria-label={`${tasks.length} taches`}>
          {tasks.length}
        </span>
      </header>
      <div className="kanban-column__tasks">
        {tasks.length === 0 ? (
          <div className="kanban-column__empty">
            <p>Deposez une tache ici</p>
          </div>
        ) : (
          tasks.map((task) => (
            <KanbanCard
              key={task.id}
              task={task}
              active={activeId === task.id}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))
        )}
      </div>
    </section>
  );
}

export function TaskKanban({ tasks, moving, onEdit, onDelete, onMove }: TaskKanbanProps) {
  const [visibleTasks, setVisibleTasks] = useState(tasks);
  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
    useSensor(KeyboardSensor),
  );

  useEffect(() => setVisibleTasks(tasks), [tasks]);

  const handleDragStart = ({ active }: DragStartEvent) => {
    setActiveTask(active.data.current?.task as Task);
  };

  const handleDragEnd = async ({ active, over }: DragEndEvent) => {
    const task = active.data.current?.task as Task | undefined;
    const nextStatus = over?.id as TaskStatus | undefined;
    setActiveTask(null);
    if (!task || !nextStatus || task.status === nextStatus) return;

    setVisibleTasks((current) =>
      current.map((item) => (item.id === task.id ? { ...item, status: nextStatus } : item)),
    );
    const succeeded = await onMove(task, nextStatus);
    if (!succeeded) {
      setVisibleTasks((current) =>
        current.map((item) => (item.id === task.id ? { ...item, status: task.status } : item)),
      );
    }
  };

  return (
    <DndContext
      sensors={sensors}
      onDragStart={handleDragStart}
      onDragCancel={() => setActiveTask(null)}
      onDragEnd={handleDragEnd}
    >
      <div className={cn("kanban-board", moving && "kanban-board--moving")}>
        {columns.map((column) => (
          <KanbanColumn
            key={column.status}
            {...column}
            tasks={visibleTasks.filter((task) => task.status === column.status)}
            activeId={activeTask?.id ?? null}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>
      <DragOverlay dropAnimation={{ duration: 260, easing: "cubic-bezier(.2,.8,.2,1)" }}>
        {activeTask ? (
          <div className="kanban-drag-overlay">
            <TaskCard task={activeTask} onEdit={() => undefined} onDelete={() => undefined} />
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
}
