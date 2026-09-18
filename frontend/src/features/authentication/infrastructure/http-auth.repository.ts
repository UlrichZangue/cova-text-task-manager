import { apiClient } from "../../../core/http/api-client";
import { endpoints } from "../../../core/http/endpoints";
import type { AuthRepository } from "../domain/auth.repository";
import type {
  AuthSession,
  LoginCredentials,
  RegisterData,
  RegistrationResult,
} from "../domain/auth.types";

export class HttpAuthRepository implements AuthRepository {
  login(credentials: LoginCredentials): Promise<AuthSession> {
    return apiClient.post<AuthSession>(endpoints.auth.login, credentials, false);
  }

  register(data: RegisterData): Promise<RegistrationResult> {
    return apiClient.post<RegistrationResult>(endpoints.auth.register, data, false);
  }
}
