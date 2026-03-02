import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { ProtectedRoute } from "./ProtectedRoute";

const useAuthMock = vi.fn();

vi.mock("../hooks/useAuth", () => ({
  useAuth: () => useAuthMock(),
}));

function renderRoute(initialEntries: string[] = ["/tickets"]) {
  const router = createMemoryRouter(
    [
      {
        path: "/login",
        element: <h1>Login Page</h1>,
      },
      {
        element: <ProtectedRoute />,
        children: [{ path: "/tickets", element: <h1>Tickets Page</h1> }],
      },
    ],
    { initialEntries },
  );

  return render(<RouterProvider router={router} />);
}

describe("ProtectedRoute", () => {
  it("redirects to /login when there is no token", async () => {
    useAuthMock.mockReturnValue({ isAuthenticated: false, isHydrated: true });

    renderRoute();

    expect(await screen.findByRole("heading", { name: "Login Page" })).toBeInTheDocument();
  });
});
