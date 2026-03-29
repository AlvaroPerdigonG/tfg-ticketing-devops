import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { vi } from "vitest";
import { RequireRole } from "./RequireRole";

const useAuthMock = vi.fn();

vi.mock("../hooks/useAuth", () => ({
  useAuth: () => useAuthMock(),
}));

function renderRoute() {
  const router = createMemoryRouter(
    [
      { path: "/forbidden", element: <h1>Forbidden</h1> },
      {
        path: "/admin",
        element: (
          <RequireRole anyOf={["ADMIN"]}>
            <h1>Admin Panel</h1>
          </RequireRole>
        ),
      },
    ],
    { initialEntries: ["/admin"] },
  );

  return render(<RouterProvider router={router} />);
}

describe("[ADMIN-04] usuario no autorizado no accede a admin", () => {
  it("redirecciona a /forbidden y oculta el contenido protegido", async () => {
    useAuthMock.mockReturnValue({ hasAnyRole: () => false });

    renderRoute();

    expect(await screen.findByRole("heading", { name: "Forbidden" })).toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: "Admin Panel" })).not.toBeInTheDocument();
  });
});
