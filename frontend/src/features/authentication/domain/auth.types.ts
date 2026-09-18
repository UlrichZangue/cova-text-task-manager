export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData extends LoginCredentials {
  name: string;
  confirmPassword: string;
}

export interface AuthSession {
  token: string;
  tokenType: "Bearer";
  expiresIn: number;
}

export interface RegistrationResult {
  userId: string;
  message: string;
}
