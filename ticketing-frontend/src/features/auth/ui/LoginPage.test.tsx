import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { ApiError } from "src/shared/api/errors";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { LoginPage } from "./LoginPage";

const loginMock = vi.fn();
const registerMock = vi.fn();

vi.mock("../hooks/useAuth", () => ({
  useAuth: () => ({
    login: (...args: unknown[]) => loginMock(...args),
    register: (...args: unknown[]) => registerMock(...args),
  }),
}));

function renderLogin(initialState?: { from?: string }) {
  const router = createMemoryRouter(
    [
      { path: "/login", element: <LoginPage /> },
      { path: "/tickets", element: <h1>Tickets</h1> },
      { path: "/", element: <h1>Home</h1> },
    ],
    { initialEntries: [{ pathname: "/login", state: initialState }] },
  );

  renderWithProviders(<RouterProvider router={router} />);
}

describe("[AUTH-02] credenciales inválidas reflejan error adecuado", () => {
  it("muestra status y mensaje devuelto por la API", async () => {
    const user = userEvent.setup();
    loginMock.mockRejectedValueOnce(new ApiError("Credenciales inválidas", 401));

    renderLogin();

    await user.type(screen.getByLabelText("Email"), "user@test.com");
    await user.type(screen.getByLabelText("Contraseña"), "bad-pass");
    await user.click(screen.getByText("Iniciar sesión", { selector: "button.auth-submit" }));

    expect(await screen.findByText("401 — Credenciales inválidas")).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Tickets" })).not.toBeInTheDocument();
  });
});

describe("[AUTH-03] usuario inactivo o no válido no queda autenticado", () => {
  it("muestra el error de usuario inactivo y no navega a contenido protegido", async () => {
    const user = userEvent.setup();
    loginMock.mockRejectedValueOnce(new ApiError("Usuario inactivo", 403));

    renderLogin({ from: "/tickets" });

    await user.type(screen.getByLabelText("Email"), "inactive@test.com");
    await user.type(screen.getByLabelText("Contraseña"), "Password.123");
    await user.click(screen.getByText("Iniciar sesión", { selector: "button.auth-submit" }));

    expect(await screen.findByText("403 — Usuario inactivo")).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Tickets" })).not.toBeInTheDocument();
  });
});
