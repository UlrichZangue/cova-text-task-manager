import { describe, expect, it } from "vitest";
import { loginSchema, registerSchema } from "./auth.schemas";

describe("authentication schemas", () => {
  it("accepts valid login credentials", () => {
    expect(
      loginSchema.safeParse({ email: "alice@example.com", password: "Password123!" }).success,
    ).toBe(true);
  });

  it("rejects invalid registration data and mismatched passwords", () => {
    const result = registerSchema.safeParse({
      name: "A",
      email: "invalid",
      password: "short",
      confirmPassword: "different",
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const fields = result.error.issues.map((issue) => issue.path[0]);
      expect(fields).toEqual(
        expect.arrayContaining(["name", "email", "password", "confirmPassword"]),
      );
    }
  });
});
