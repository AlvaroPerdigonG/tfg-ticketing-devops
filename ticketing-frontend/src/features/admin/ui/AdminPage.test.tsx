import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { message } from "antd";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { vi } from "vitest";
import { AdminPage } from "./AdminPage";

const getCategoriesMock = vi.fn();
const createCategoryMock = vi.fn();
const updateCategoryMock = vi.fn();
const getUsersMock = vi.fn();
const updateUserActiveMock = vi.fn();

vi.mock("../api/adminApi", () => ({
  adminApi: {
    getCategories: () => getCategoriesMock(),
    createCategory: (name: string) => createCategoryMock(name),
    updateCategory: (categoryId: string, payload: { name: string; isActive: boolean }) =>
      updateCategoryMock(categoryId, payload),
    getUsers: () => getUsersMock(),
    updateUserActive: (userId: string, isActive: boolean) => updateUserActiveMock(userId, isActive),
  },
}));

describe("AdminPage", () => {
  const messageSuccessSpy = vi
    .spyOn(message, "success")
    .mockImplementation(() => ({}) as ReturnType<typeof message.success>);
  const messageErrorSpy = vi
    .spyOn(message, "error")
    .mockImplementation(() => ({}) as ReturnType<typeof message.error>);

  beforeEach(() => {
    getCategoriesMock.mockReset();
    createCategoryMock.mockReset();
    updateCategoryMock.mockReset();
    getUsersMock.mockReset();
    updateUserActiveMock.mockReset();
    messageSuccessSpy.mockClear();
    messageErrorSpy.mockClear();

    getCategoriesMock.mockResolvedValue([{ id: "cat-1", name: "Software", isActive: true }]);
    getUsersMock.mockResolvedValue([
      {
        id: "u1",
        email: "user@test.com",
        displayName: "Regular User",
        role: "USER",
        isActive: true,
      },
    ]);
  });

  it("renderiza tabs y datos cargados en categorías", async () => {
    renderWithProviders(<AdminPage />);

    expect(screen.getByRole("heading", { name: "Administration" })).toBeInTheDocument();
    expect(await screen.findByText("Software")).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Users" })).toBeInTheDocument();
  });

  it("crea una categoría nueva y limpia el input", async () => {
    createCategoryMock.mockResolvedValue({ id: "cat-2", name: "Hardware", isActive: true });

    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    const input = await screen.findByPlaceholderText("New category");
    await user.type(input, "Hardware");
    await user.click(screen.getByRole("button", { name: "Create" }));

    await waitFor(() => {
      expect(createCategoryMock).toHaveBeenCalledWith("Hardware");
      expect(screen.getByText("Hardware")).toBeInTheDocument();
      expect(screen.getByPlaceholderText("New category")).toHaveValue("");
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("Category created");
  });

  it("edita categoría existente (nombre + activa/inactiva)", async () => {
    updateCategoryMock.mockResolvedValue({ id: "cat-1", name: "Software Legacy", isActive: false });

    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    await user.click(await screen.findByRole("button", { name: "Edit" }));

    const nameInput = await screen.findByPlaceholderText("Name");
    await user.clear(nameInput);
    await user.type(nameInput, "Software Legacy");

    const modalSwitch = screen.getAllByRole("switch")[0];
    await user.click(modalSwitch);

    await user.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() => {
      expect(updateCategoryMock).toHaveBeenCalledWith("cat-1", {
        name: "Software Legacy",
        isActive: false,
      });
      expect(screen.getByText("Software Legacy")).toBeInTheDocument();
      expect(screen.getByText("Inactive")).toBeInTheDocument();
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("Category updated");
  });

  it("deactiva usuario desde tab de usuarios", async () => {
    updateUserActiveMock.mockResolvedValue({
      id: "u1",
      email: "user@test.com",
      displayName: "Regular User",
      role: "USER",
      isActive: false,
    });

    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    await user.click(screen.getByRole("tab", { name: "Users" }));
    const userSwitch = await screen.findByRole("switch");
    await user.click(userSwitch);

    await waitFor(() => {
      expect(updateUserActiveMock).toHaveBeenCalledWith("u1", false);
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("User status updated");
  });

  it("muestra error cuando falla la carga inicial", async () => {
    getCategoriesMock.mockRejectedValueOnce(new Error("boom"));
    getUsersMock.mockRejectedValueOnce(new Error("boom"));

    renderWithProviders(<AdminPage />);

    await waitFor(() => {
      expect(messageErrorSpy).toHaveBeenCalledWith("Could not load categories");
      expect(messageErrorSpy).toHaveBeenCalledWith("Could not load users");
    });
  });

  it("muestra error si se intenta guardar categoría con nombre vacío", async () => {
    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    await user.click(await screen.findByRole("button", { name: "Edit" }));
    const nameInput = await screen.findByPlaceholderText("Name");
    await user.clear(nameInput);

    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(messageErrorSpy).toHaveBeenCalledWith("Name is required");
    expect(updateCategoryMock).not.toHaveBeenCalled();
  });
});
