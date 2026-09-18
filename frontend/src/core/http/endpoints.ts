export const endpoints = {
  auth: {
    login: "/api/auth/login",
    register: "/api/auth/register",
  },
  users: {
    me: "/api/users/me",
  },
  tasks: {
    all: "/api/tasks",
    stats: "/api/tasks/stats",
    byId: (id: string) => `/api/tasks/${id}`,
  },
} as const;
