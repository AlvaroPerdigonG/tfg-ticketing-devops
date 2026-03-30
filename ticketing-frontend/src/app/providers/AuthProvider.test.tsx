import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { useAuth } from "src/features/auth/hooks/useAuth";
import { AuthProvider } from "./AuthProvider";
import { renderWithProviders } from "src/test/utils/renderWithProviders";
import { buildJwt } from "src/test/utils/authTestUtils";

const loginMock = vi.fn();

vi.mock("src/features/auth/api/authApi", () => ({
  authApi: {
    login: (...args: unknown[]) => loginMock(...args),
    register: vi.fn(),
    me: vi.fn(),
  },
}));

function AuthStateProbe() {
  const { isAuthenticated, state, login } = useAuth();

  return (
    <section>
      <p>Estado: {isAuthenticated ? "autenticado" : "anonimo"}</p>
      <p>Usuario: {state.user?.email ?? "sin usuario"}</p>
      <button type="button" onClick={() => login("admin@test.com", "Password.123", true)}>
        Login
      </button>
    </section>
  );
}

describe("[AUTH-01] login correcto refleja estado autenticado", () => {
  it("actualiza el estado visible de sesión cuando el backend devuelve un token válido", async () => {
    const user = userEvent.setup();

    loginMock.mockResolvedValue({
      accessToken: buildJwt({
        sub: "u-1",
        email: "admin@test.com",
        displayName: "Admin",
        roles: ["ADMIN"],
        exp: Math.floor(Date.now() / 1000) + 3600,
      }),
    });

    renderWithProviders(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>,
    );

    expect(screen.getByText("Estado: anonimo")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Estado: autenticado")).toBeInTheDocument();
    expect(screen.getByText("Usuario: admin@test.com")).toBeInTheDocument();
  });
});
