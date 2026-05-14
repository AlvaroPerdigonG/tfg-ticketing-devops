import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
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
      <p>Status: {isAuthenticated ? "authenticated" : "anonymous"}</p>
      <p>User: {state.user?.email ?? "no user"}</p>
      <button type="button" onClick={() => login("admin@test.com", "Password.123", true)}>
        Login
      </button>
    </section>
  );
}

function AuthStateProbeSession() {
  const { isAuthenticated, state, login } = useAuth();

  return (
    <section>
      <p>Session status: {isAuthenticated ? "authenticated" : "anonymous"}</p>
      <p>Session user: {state.user?.email ?? "no user"}</p>
      <button type="button" onClick={() => login("agent@test.com", "Password.123", false)}>
        Session login
      </button>
    </section>
  );
}

beforeEach(() => {
  localStorage.clear();
  sessionStorage.clear();
  loginMock.mockReset();
});

describe("AUTH-01 Correct login", () => {
  it("updates the visible session state when the backend returns a valid token", async () => {
    const user = userEvent.setup();

    const token = buildJwt({
      sub: "u-1",
      email: "admin@test.com",
      displayName: "Admin",
      roles: ["ADMIN"],
      exp: Math.floor(Date.now() / 1000) + 3600,
    });

    loginMock.mockResolvedValue({
      accessToken: token,
    });

    renderWithProviders(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>,
    );

    expect(screen.getByText("Status: anonymous")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Status: authenticated")).toBeInTheDocument();
    expect(screen.getByText("User: admin@test.com")).toBeInTheDocument();
    expect(localStorage.getItem("ticketing_access_token")).toBe(token);
    expect(sessionStorage.getItem("ticketing_access_token")).toBeNull();
  });

  it("with remember=false stores token in sessionStorage and not in localStorage", async () => {
    const user = userEvent.setup();

    const token = buildJwt({
      sub: "u-2",
      email: "agent@test.com",
      displayName: "Agent",
      roles: ["AGENT"],
      exp: Math.floor(Date.now() / 1000) + 3600,
    });

    loginMock.mockResolvedValue({
      accessToken: token,
    });

    renderWithProviders(
      <AuthProvider>
        <AuthStateProbeSession />
      </AuthProvider>,
    );

    await user.click(screen.getByRole("button", { name: "Session login" }));

    expect(await screen.findByText("Session status: authenticated")).toBeInTheDocument();
    expect(localStorage.getItem("ticketing_access_token")).toBeNull();
    expect(sessionStorage.getItem("ticketing_access_token")).toBe(token);
  });
});
