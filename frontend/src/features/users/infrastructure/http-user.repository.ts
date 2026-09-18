import { apiClient } from "../../../core/http/api-client";
import { endpoints } from "../../../core/http/endpoints";
import type { UserRepository } from "../domain/user.repository";
import type { UserProfile } from "../domain/user.types";

export class HttpUserRepository implements UserRepository {
  getCurrent(): Promise<UserProfile> {
    return apiClient.get<UserProfile>(endpoints.users.me);
  }
}
