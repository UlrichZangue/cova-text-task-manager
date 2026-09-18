import { HttpAuthRepository } from "../infrastructure/http-auth.repository";
import { LoginUseCase, RegisterUseCase } from "./auth.use-cases";

const repository = new HttpAuthRepository();

export const loginUseCase = new LoginUseCase(repository);
export const registerUseCase = new RegisterUseCase(repository);
