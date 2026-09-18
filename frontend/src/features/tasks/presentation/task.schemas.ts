import { z } from "zod";

export const taskSchema = z.object({
  title: z.string().trim().min(1, "Le titre est obligatoire.").max(150, "150 caracteres maximum."),
  description: z.string().max(2000, "2000 caracteres maximum."),
  status: z.enum(["TODO", "IN_PROGRESS", "DONE"]),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"]),
  dueDate: z.string(),
});

export type TaskFormData = z.infer<typeof taskSchema>;
