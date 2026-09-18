export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, payload?: ApiErrorPayload) {
    super(payload?.message || "Une erreur inattendue est survenue.");
    this.name = "ApiError";
    this.status = status;
    this.code = payload?.error;
    this.fieldErrors = payload?.fieldErrors ?? {};
  }
}
