import { ArrowRight, CheckCircle2, CircleDot, Clock3, ListTodo, Plus } from "lucide-react";
import { Link } from "react-router-dom";
import { StateView } from "../../../shared/components/StateView";
import { formatDate } from "../../../shared/lib/date";
import { useCurrentUser } from "../../users/presentation/use-current-user";
import { statusLabels } from "../../tasks/presentation/task.constants";
import { useTasks, useTaskStats } from "../../tasks/presentation/task.queries";

export function DashboardPage() {
  const user = useCurrentUser();
  const stats = useTaskStats();
  const recent = useTasks({
    search: "",
    status: "ALL",
    priority: "ALL",
    page: 0,
    size: 5,
    sort: "createdAt,desc",
  });

  if (stats.isLoading || recent.isLoading)
    return <StateView type="loading" message="Preparation de votre tableau de bord..." />;
  if (stats.isError || recent.isError)
    return (
      <StateView
        type="error"
        message="Le tableau de bord ne peut pas etre charge."
        actionLabel="Reessayer"
        onAction={() => {
          stats.refetch();
          recent.refetch();
        }}
      />
    );

  const cards = [
    { label: "Total", value: stats.data!.total, icon: ListTodo, tone: "ink" },
    { label: "A faire", value: stats.data!.todo, icon: CircleDot, tone: "coral" },
    { label: "En cours", value: stats.data!.inProgress, icon: Clock3, tone: "amber" },
    { label: "Terminees", value: stats.data!.done, icon: CheckCircle2, tone: "green" },
  ];

  return (
    <div className="page-content">
      <header className="page-header page-header--actions">
        <div>
          <p className="eyebrow">Aujourd'hui</p>
          <h1>Bonjour{user.data?.name ? `, ${user.data.name.split(" ")[0]}` : ""}</h1>
          <p>Voici une vue claire de ce qui merite votre attention.</p>
        </div>
        <Link className="button button--primary button--md" to="/tasks?create=true">
          <Plus /> Nouvelle tache
        </Link>
      </header>
      <section className="stats-grid" aria-label="Statistiques des taches">
        {cards.map(({ label, value, icon: Icon, tone }) => (
          <article className="stat-card" key={label}>
            <div className={`stat-card__icon stat-card__icon--${tone}`}>
              <Icon />
            </div>
            <div>
              <p>{label}</p>
              <strong>{value}</strong>
            </div>
          </article>
        ))}
      </section>
      <section className="dashboard-section">
        <div className="section-heading">
          <div>
            <h2>Activite recente</h2>
            <p>Vos cinq dernieres taches creees.</p>
          </div>
          <Link to="/tasks">
            Voir toutes <ArrowRight />
          </Link>
        </div>
        {recent.data!.content.length === 0 ? (
          <StateView
            type="empty"
            title="Aucune tache pour le moment"
            message="Votre activite apparaitra ici."
          />
        ) : (
          <div className="recent-list">
            {recent.data!.content.map((task) => (
              <article className="recent-row" key={task.id}>
                <span
                  className={`recent-row__marker recent-row__marker--${task.status.toLowerCase()}`}
                />
                <div className="recent-row__main">
                  <h3>{task.title}</h3>
                  <p>{task.description || "Aucune description"}</p>
                </div>
                <span className={`status-badge status-badge--${task.status.toLowerCase()}`}>
                  {statusLabels[task.status]}
                </span>
                <time>{formatDate(task.dueDate)}</time>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
