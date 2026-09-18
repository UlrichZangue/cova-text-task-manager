import type { UserProfile } from "./user.types";

export interface UserRepository {
  getCurrent(): Promise<UserProfile>;
}
