import { useMutation } from "@tanstack/react-query";
import { loginUseCase, registerUseCase } from "../application/auth.dependencies";

export function useLoginMutation() {
  return useMutation({ mutationFn: loginUseCase.execute.bind(loginUseCase) });
}

export function useRegisterMutation() {
  return useMutation({ mutationFn: registerUseCase.execute.bind(registerUseCase) });
}
