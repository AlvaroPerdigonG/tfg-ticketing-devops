type Role = "user" | "agent" | "admin";

type Credentials = {
  email: string;
  password: string;
};

const credentialsByRole: Record<Role, Credentials> = {
  user: {
    email: process.env.E2E_USER_EMAIL ?? "user@local.test",
    password: process.env.E2E_USER_PASSWORD ?? "password",
  },
  agent: {
    email: process.env.E2E_AGENT_EMAIL ?? "agent@local.test",
    password: process.env.E2E_AGENT_PASSWORD ?? "password",
  },
  admin: {
    email: process.env.E2E_ADMIN_EMAIL ?? "",
    password: process.env.E2E_ADMIN_PASSWORD ?? "",
  },
};

export function getCredentials(role: Role): Credentials {
  return credentialsByRole[role];
}

export function hasAdminCredentials() {
  const admin = credentialsByRole.admin;
  return Boolean(admin.email && admin.password);
}
