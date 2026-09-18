import type { UserRepository } from "../domain/user.repository";

export class GetCurrentUserUseCase {
  constructor(private readonly repository: UserRepository) {}
  execute() {
    return this.repository.getCurrent();
  }
}
