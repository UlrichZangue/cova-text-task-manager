import {
  CheckCircle2,
  LayoutDashboard,
  ListTodo,
  LogOut,
  Menu,
  Moon,
  Sun,
  UserRound,
  X,
} from "lucide-react";
import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { Button } from "../shared/components/Button";
import { useTheme } from "../shared/providers/use-theme";
import { useAuth } from "../features/authentication/presentation/use-auth";
import { useCurrentUser } from "../features/users/presentation/use-current-user";

const navigation = [
  { to: "/dashboard", label: "Tableau de bord", icon: LayoutDashboard },
  { to: "/tasks", label: "Mes taches", icon: ListTodo },
  { to: "/profile", label: "Mon profil", icon: UserRound },
];

export function AppShell() {
  const [menuOpen, setMenuOpen] = useState(false);
  const { theme, toggleTheme } = useTheme();
  const { signOut } = useAuth();
  const user = useCurrentUser();
  const navigate = useNavigate();
  const logout = () => {
    signOut();
    navigate("/login", { replace: true });
  };

  return (
    <div className="app-shell">
      <header className="mobile-header">
        <div className="brand">
          <span className="brand__mark">
            <CheckCircle2 />
          </span>
          <span>Taskflow</span>
        </div>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setMenuOpen((value) => !value)}
          aria-label="Ouvrir le menu"
        >
          {menuOpen ? <X /> : <Menu />}
        </Button>
      </header>
      <aside className={`sidebar ${menuOpen ? "sidebar--open" : ""}`}>
        <div className="brand">
          <span className="brand__mark">
            <CheckCircle2 />
          </span>
          <span>Taskflow</span>
        </div>
        <nav className="sidebar__nav">
          {navigation.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) => (isActive ? "nav-link nav-link--active" : "nav-link")}
            >
              <Icon />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="sidebar__footer">
          <button className="theme-toggle" onClick={toggleTheme}>
            {theme === "light" ? <Moon /> : <Sun />}
            <span>{theme === "light" ? "Mode sombre" : "Mode clair"}</span>
          </button>
          <div className="user-menu">
            <div className="user-menu__avatar">
              {user.data?.name.charAt(0).toUpperCase() ?? "U"}
            </div>
            <div className="user-menu__text">
              <strong>{user.data?.name ?? "Utilisateur"}</strong>
              <span>{user.data?.email ?? "Chargement..."}</span>
            </div>
            <Button variant="ghost" size="icon" onClick={logout} aria-label="Se deconnecter">
              <LogOut />
            </Button>
          </div>
        </div>
      </aside>
      {menuOpen && (
        <button
          className="sidebar-overlay"
          aria-label="Fermer le menu"
          onClick={() => setMenuOpen(false)}
        />
      )}
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
