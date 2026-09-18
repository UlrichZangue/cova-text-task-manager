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
import { registerSchema, type RegisterFormData } from "./auth.schemas";
import { useRegisterMutation } from "./use-auth-mutations";

export function RegisterPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const mutation = useRegisterMutation();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: "", email: "", password: "", confirmPassword: "" },
  });

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutation.mutateAsync(values);
      toast.success("Compte cree. Vous pouvez vous connecter.");
      navigate("/login", { replace: true });
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fieldErrors).forEach(([field, message]) => {
          setError(field as keyof RegisterFormData, { message });
        });
        toast.error(error.message);
      } else toast.error("Inscription impossible.");
    }
  });

  return (
    <AuthLayout>
      <div className="auth-form-wrap">
        <div className="auth-form-heading">
          <p className="eyebrow">Nouveau depart</p>
          <h2>Creez votre espace</h2>
          <p>Quelques secondes suffisent pour commencer a organiser vos taches.</p>
        </div>
        <form onSubmit={onSubmit} className="form-stack" noValidate>
          <InputField
            label="Nom complet"
            autoComplete="name"
            placeholder="Alice Dupont"
            error={errors.name?.message}
            {...register("name")}
          />
          <InputField
            label="Adresse email"
            type="email"
            autoComplete="email"
            placeholder="vous@exemple.com"
            error={errors.email?.message}
            {...register("email")}
          />
          <div className="form-grid">
            <InputField
              label="Mot de passe"
              type="password"
              autoComplete="new-password"
              error={errors.password?.message}
              {...register("password")}
            />
            <InputField
              label="Confirmation"
              type="password"
              autoComplete="new-password"
              error={errors.confirmPassword?.message}
              {...register("confirmPassword")}
            />
          </div>
          <Button type="submit" loading={mutation.isPending} className="button--full">
            Creer mon compte <ArrowRight aria-hidden="true" />
          </Button>
        </form>
        <p className="auth-switch">
          Vous avez deja un compte ? <Link to="/login">Se connecter</Link>
        </p>
      </div>
    </AuthLayout>
  );
}
