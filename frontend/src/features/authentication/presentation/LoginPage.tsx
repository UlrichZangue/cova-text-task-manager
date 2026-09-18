import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { ApiError } from "../../../core/http/api-error";
import { Button } from "../../../shared/components/Button";
import { InputField } from "../../../shared/components/FormField";
import { AuthLayout } from "./AuthLayout";
import { useAuth } from "./use-auth";
import { loginSchema, type LoginFormData } from "./auth.schemas";
import { useLoginMutation } from "./use-auth-mutations";

export function LoginPage() {
  const { isAuthenticated, signIn } = useAuth();
  const navigate = useNavigate();
  const mutation = useLoginMutation();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const onSubmit = handleSubmit(async (values) => {
    try {
      const session = await mutation.mutateAsync(values);
      signIn(session.token);
      toast.success("Connexion reussie");
      navigate("/dashboard", { replace: true });
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Connexion impossible.");
    }
  });

  return (
    <AuthLayout>
      <div className="auth-form-wrap">
        <div className="auth-form-heading">
          <p className="eyebrow">Bon retour</p>
          <h2>Connectez-vous</h2>
          <p>Retrouvez vos priorites et reprenez la ou vous vous etiez arrete.</p>
        </div>
        <form onSubmit={onSubmit} className="form-stack" noValidate>
          <InputField
            label="Adresse email"
            type="email"
            autoComplete="email"
            placeholder="vous@exemple.com"
            error={errors.email?.message}
            {...register("email")}
          />
          <InputField
            label="Mot de passe"
            type="password"
            autoComplete="current-password"
            placeholder="Votre mot de passe"
            error={errors.password?.message}
            {...register("password")}
          />
          <Button type="submit" loading={mutation.isPending} className="button--full">
            Se connecter <ArrowRight aria-hidden="true" />
          </Button>
        </form>
        <p className="auth-switch">
          Pas encore de compte ? <Link to="/register">Creer un compte</Link>
        </p>
      </div>
    </AuthLayout>
  );
}
