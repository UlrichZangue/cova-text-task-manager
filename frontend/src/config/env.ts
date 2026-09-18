const rawApiUrl = import.meta.env.VITE_API_URL?.trim() || "http://localhost:8080";

export const env = {
  apiUrl: rawApiUrl.replace(/\/$/, ""),
} as const;
