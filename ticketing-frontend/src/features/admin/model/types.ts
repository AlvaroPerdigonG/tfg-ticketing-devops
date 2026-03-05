export type AdminCategory = {
  id: string;
  name: string;
  isActive: boolean;
};

export type AdminUser = {
  id: string;
  email: string;
  displayName: string;
  role: "USER" | "AGENT" | "ADMIN";
  isActive: boolean;
};
