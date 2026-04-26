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

    expect(screen.getByRole("heading", { name: "Administración" })).toBeInTheDocument();
    expect(await screen.findByText("Software")).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Usuarios" })).toBeInTheDocument();
  });

  it("crea una categoría nueva y limpia el input", async () => {
    createCategoryMock.mockResolvedValue({ id: "cat-2", name: "Hardware", isActive: true });

    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    const input = await screen.findByPlaceholderText("Nueva categoría");
    await user.type(input, "Hardware");
    await user.click(screen.getByRole("button", { name: "Crear" }));

    await waitFor(() => {
      expect(createCategoryMock).toHaveBeenCalledWith("Hardware");
      expect(screen.getByText("Hardware")).toBeInTheDocument();
      expect(screen.getByPlaceholderText("Nueva categoría")).toHaveValue("");
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("Categoría creada");
  });

  it("edita categoría existente (nombre + activa/inactiva)", async () => {
    updateCategoryMock.mockResolvedValue({ id: "cat-1", name: "Software Legacy", isActive: false });

    const user = userEvent.setup();
    renderWithProviders(<AdminPage />);

    await user.click(await screen.findByRole("button", { name: "Editar" }));

    const nameInput = await screen.findByPlaceholderText("Nombre");
    await user.clear(nameInput);
    await user.type(nameInput, "Software Legacy");

    const modalSwitch = screen.getAllByRole("switch")[0];
    await user.click(modalSwitch);

    await user.click(screen.getByRole("button", { name: "Guardar" }));

    await waitFor(() => {
      expect(updateCategoryMock).toHaveBeenCalledWith("cat-1", {
        name: "Software Legacy",
        isActive: false,
      });
      expect(screen.getByText("Software Legacy")).toBeInTheDocument();
      expect(screen.getByText("Inactiva")).toBeInTheDocument();
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("Categoría actualizada");
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

    await user.click(screen.getByRole("tab", { name: "Usuarios" }));
    const userSwitch = await screen.findByRole("switch");
    await user.click(userSwitch);

    await waitFor(() => {
      expect(updateUserActiveMock).toHaveBeenCalledWith("u1", false);
    });
    expect(messageSuccessSpy).toHaveBeenCalledWith("Estado de usuario actualizado");
  });

  it("muestra error cuando falla la carga inicial", async () => {
    getCategoriesMock.mockRejectedValueOnce(new Error("boom"));
    getUsersMock.mockRejectedValueOnce(new Error("boom"));

    renderWithProviders(<AdminPage />);

    await waitFor(() => {
      expect(messageErrorSpy).toHaveBeenCalledWith("No se pudieron cargar las categorías");
      expect(messageErrorSpy).toHaveBeenCalledWith("No se pudieron cargar los usuarios");
    });
  });
});
