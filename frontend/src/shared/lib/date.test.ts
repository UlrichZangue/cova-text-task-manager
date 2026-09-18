import { describe, expect, it, vi } from "vitest";
import { formatDate, isOverdue } from "./date";

describe("date helpers", () => {
  it("formats missing due dates", () => {
    expect(formatDate(null)).toBe("Sans echeance");
  });

  it("detects overdue dates", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-09-18T12:00:00"));
    expect(isOverdue("2026-09-17")).toBe(true);
    expect(isOverdue("2026-09-19")).toBe(false);
    vi.useRealTimers();
  });
});
