import { z } from "zod";

export const loginSchema = z.object({
  email: z.email("Saisissez une adresse email valide."),
  password: z.string().min(1, "Le mot de passe est obligatoire."),
});

export const registerSchema = z
  .object({
    name: z.string().trim().min(2, "Le nom doit contenir au moins 2 caracteres.").max(100),
    email: z.email("Saisissez une adresse email valide."),
    password: z.string().min(8, "Le mot de passe doit contenir au moins 8 caracteres.").max(72),
    confirmPassword: z.string().min(1, "Confirmez votre mot de passe."),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Les mots de passe ne correspondent pas.",
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
