import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { tokenStorage } from "../../../core/auth/token-storage";
import { AuthContext } from "./auth-context";

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [isAuthenticated, setAuthenticated] = useState(() => Boolean(tokenStorage.get()));

  const signOut = useCallback(() => {
    tokenStorage.clear();
    setAuthenticated(false);
    queryClient.clear();
  }, [queryClient]);

  const signIn = useCallback((token: string) => {
    tokenStorage.set(token);
    setAuthenticated(true);
  }, []);

  useEffect(() => {
    window.addEventListener("auth:unauthorized", signOut);
    return () => window.removeEventListener("auth:unauthorized", signOut);
  }, [signOut]);

  const value = useMemo(
    () => ({ isAuthenticated, signIn, signOut }),
    [isAuthenticated, signIn, signOut],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
