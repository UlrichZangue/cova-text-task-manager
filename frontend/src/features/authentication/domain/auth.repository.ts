import type { AuthSession, LoginCredentials, RegisterData, RegistrationResult } from "./auth.types";

export interface AuthRepository {
  login(credentials: LoginCredentials): Promise<AuthSession>;
  register(data: RegisterData): Promise<RegistrationResult>;
}
