import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { beforeEach, vi } from "vitest";
import { AuthProvider } from "src/app/providers/AuthProvider";
import { ApiError } from "src/shared/api/errors";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { buildJwt } from "src/test/utils/authTestUtils";
import { LoginPage } from "./LoginPage";

const loginMock = vi.fn();

vi.mock("../api/authApi", () => ({
  authApi: {
    login: (...args: unknown[]) => loginMock(...args),
    register: vi.fn(),
    me: vi.fn(),
  },
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

  renderWithProviders(
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>,
  );
}

function getSubmitButton(name: RegExp | string = /^Iniciar sesión$/i) {
  const buttons = screen.getAllByRole("button", { name });
  const submit = buttons.find((button) => button.getAttribute("type") === "submit");

  if (!submit) throw new Error("No se encontró el botón submit del formulario de login");
  return submit;
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });

  return { promise, resolve, reject };
}

beforeEach(() => {
  localStorage.clear();
  sessionStorage.clear();
  loginMock.mockReset();
});

describe("[AUTH-01] Login correcto", () => {
  it("envía credenciales válidas y navega al destino protegido tras autenticarse", async () => {
    const user = userEvent.setup();

    loginMock.mockResolvedValueOnce({
      accessToken: buildJwt({
        sub: "u1",
        email: "user@test.com",
        displayName: "Usuario",
        roles: ["USER"],
        exp: Math.floor(Date.now() / 1000) + 3600,
      }),
    });

    renderLogin({ from: "/tickets" });

    await user.type(screen.getByLabelText("Email"), "user@test.com");
    await user.type(screen.getByLabelText("Contraseña"), "Password.123");
    await user.click(getSubmitButton());

    expect(loginMock).toHaveBeenCalledWith({ email: "user@test.com", password: "Password.123" });
    expect(await screen.findByRole("heading", { name: "Tickets" })).toBeInTheDocument();
  });

  it("muestra estado loading durante el envío", async () => {
    const user = userEvent.setup();
    const request = deferred<{ accessToken: string }>();

    loginMock.mockReturnValueOnce(request.promise);

    renderLogin();

    await user.type(screen.getByLabelText("Email"), "user@test.com");
    await user.type(screen.getByLabelText("Contraseña"), "Password.123");
    await user.click(getSubmitButton());

    const submit = await screen.findByRole("button", { name: "Procesando..." });
    expect(submit).toBeDisabled();

    request.resolve({
      accessToken: buildJwt({
        sub: "u1",
        email: "user@test.com",
        displayName: "Usuario",
        roles: ["USER"],
        exp: Math.floor(Date.now() / 1000) + 3600,
      }),
    });

    expect(await screen.findByRole("heading", { name: "Home" })).toBeInTheDocument();
  });

  it("aplica validación visible del formulario cuando faltan campos requeridos", async () => {
    const user = userEvent.setup();

    renderLogin();

    await user.click(getSubmitButton());

    expect(loginMock).not.toHaveBeenCalled();
    expect(screen.getByRole("heading", { name: "Ticketing Platform" })).toBeInTheDocument();
  });
});

describe("[AUTH-02] Login inválido", () => {
  it("muestra feedback de error y mantiene al usuario en login", async () => {
    const user = userEvent.setup();

    loginMock.mockRejectedValueOnce(new ApiError("Credenciales inválidas", 401));

    renderLogin({ from: "/tickets" });

    await user.type(screen.getByLabelText("Email"), "user@test.com");
    await user.type(screen.getByLabelText("Contraseña"), "wrong-pass");
    await user.click(getSubmitButton());

    expect(await screen.findByText("401 — Credenciales inválidas")).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Tickets" })).not.toBeInTheDocument();
    expect(getSubmitButton()).toBeEnabled();
  });
});
