import { useQuery } from "@tanstack/react-query";
import { GetCurrentUserUseCase } from "../application/get-current-user";
import { HttpUserRepository } from "../infrastructure/http-user.repository";

const getCurrentUser = new GetCurrentUserUseCase(new HttpUserRepository());

export const userKeys = { current: ["users", "current"] as const };

export function useCurrentUser() {
  return useQuery({
    queryKey: userKeys.current,
    queryFn: () => getCurrentUser.execute(),
    staleTime: 5 * 60 * 1000,
  });
}
