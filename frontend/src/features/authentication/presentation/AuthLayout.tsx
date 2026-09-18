import type { ReactNode } from "react";
import { CheckCircle2, ListTodo } from "lucide-react";

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <main className="auth-page">
      <section className="auth-brand" aria-label="Taskflow">
        <div className="brand-mark">
          <CheckCircle2 aria-hidden="true" />
        </div>
        <div className="auth-brand__content">
          <p className="eyebrow">Taskflow</p>
          <h1>Votre travail, clairement organise.</h1>
          <p>Planifiez, priorisez et avancez sans perdre le fil.</p>
        </div>
        <div className="auth-brand__preview" aria-hidden="true">
          <div className="preview-line">
            <ListTodo />
            <span>Finaliser le rapport</span>
            <strong>En cours</strong>
          </div>
          <div className="preview-line">
            <CheckCircle2 />
            <span>Revue hebdomadaire</span>
            <strong>Terminee</strong>
          </div>
        </div>
      </section>
      <section className="auth-panel">{children}</section>
    </main>
  );
}
