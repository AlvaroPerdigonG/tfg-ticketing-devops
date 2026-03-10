import { render, screen } from "@testing-library/react";
import { ConfigProvider } from "antd";
import { AdminPage } from "./AdminPage";
import { vi } from "vitest";

const getCategoriesMock = vi.fn();
const getUsersMock = vi.fn();

vi.mock("../api/adminApi", () => ({
  adminApi: {
    getCategories: () => getCategoriesMock(),
    createCategory: vi.fn(),
    updateCategory: vi.fn(),
    getUsers: () => getUsersMock(),
    updateUserActive: vi.fn(),
  },
}));

describe("AdminPage", () => {
  it("renders admin tabs and loaded records", async () => {
    getCategoriesMock.mockResolvedValue([{ id: "1", name: "Software", isActive: true }]);
    getUsersMock.mockResolvedValue([
      { id: "u1", email: "admin@test.com", displayName: "Admin", role: "ADMIN", isActive: true },
    ]);

    render(
      <ConfigProvider>
        <AdminPage />
      </ConfigProvider>,
    );

    expect(screen.getByRole("heading", { name: "Administración" })).toBeInTheDocument();
    expect(await screen.findByText("Software")).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Usuarios" })).toBeInTheDocument();
  });
});
