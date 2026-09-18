import type { AuthRepository } from "../domain/auth.repository";
import type { LoginCredentials, RegisterData } from "../domain/auth.types";

export class LoginUseCase {
  constructor(private readonly repository: AuthRepository) {}
  execute(credentials: LoginCredentials) {
    return this.repository.login(credentials);
  }
}

export class RegisterUseCase {
  constructor(private readonly repository: AuthRepository) {}
  execute(data: RegisterData) {
    return this.repository.register(data);
  }
}
