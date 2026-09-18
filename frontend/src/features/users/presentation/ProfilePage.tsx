import { CalendarDays, Mail, UserRound } from "lucide-react";
import { StateView } from "../../../shared/components/StateView";
import { useCurrentUser } from "./use-current-user";

export function ProfilePage() {
  const profile = useCurrentUser();
  if (profile.isLoading)
    return <StateView type="loading" message="Chargement de votre profil..." />;
  if (profile.isError)
    return (
      <StateView
        type="error"
        message="Votre profil ne peut pas etre charge."
        actionLabel="Reessayer"
        onAction={() => profile.refetch()}
      />
    );

  const user = profile.data!;
  return (
    <div className="page-content page-content--narrow">
      <header className="page-header">
        <div>
          <p className="eyebrow">Compte</p>
          <h1>Mon profil</h1>
          <p>Les informations associees a votre espace Taskflow.</p>
        </div>
      </header>
      <section className="profile-panel">
        <div className="profile-avatar" aria-hidden="true">
          {user.name.charAt(0).toUpperCase()}
        </div>
        <div className="profile-heading">
          <h2>{user.name}</h2>
          <p>Membre Taskflow</p>
        </div>
        <dl className="profile-details">
          <div>
            <dt>
              <UserRound /> Identifiant
            </dt>
            <dd>{user.id}</dd>
          </div>
          <div>
            <dt>
              <Mail /> Adresse email
            </dt>
            <dd>{user.email}</dd>
          </div>
          <div>
            <dt>
              <CalendarDays /> Membre depuis
            </dt>
            <dd>
              {new Intl.DateTimeFormat("fr-FR", { dateStyle: "long" }).format(
                new Date(user.createdAt),
              )}
            </dd>
          </div>
        </dl>
      </section>
    </div>
  );
}
