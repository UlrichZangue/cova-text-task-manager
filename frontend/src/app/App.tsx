import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "../features/authentication/presentation/LoginPage";
import { RegisterPage } from "../features/authentication/presentation/RegisterPage";
import { DashboardPage } from "../features/dashboard/presentation/DashboardPage";
import { TasksPage } from "../features/tasks/presentation/TasksPage";
import { ProfilePage } from "../features/users/presentation/ProfilePage";
import { AppProviders } from "./providers";
import { AppShell } from "./AppShell";
import { ProtectedRoute } from "./ProtectedRoute";

export function App() {
  return (
    <AppProviders>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/tasks" element={<TasksPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AppProviders>
  );
}
