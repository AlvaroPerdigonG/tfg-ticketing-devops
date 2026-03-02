import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ConfigProvider } from "antd";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import { AppShell } from "./AppShell";

function renderWithRouter(initialEntries: string[] = ["/dashboard"]) {
  const router = createMemoryRouter(
    [
      {
        element: <AppShell />,
        children: [
          { path: "/dashboard", element: <h1>Dashboard page</h1> },
          { path: "/tickets", element: <h1>Tickets page</h1> },
          { path: "/tickets/new", element: <h1>Nuevo ticket page</h1> },
        ],
      },
    ],
    { initialEntries },
  );

  return render(
    <ConfigProvider>
      <RouterProvider router={router} />
    </ConfigProvider>,
  );
}

describe("AppShell", () => {
  it("renders sidebar, header and page content", () => {
    renderWithRouter();

    expect(screen.getByText("TFG Ticketing")).toBeInTheDocument();
    expect(screen.getByText("Ticketing Platform")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Dashboard page" })).toBeInTheDocument();
  });

  it("navigates from sidebar links", async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText("Tickets"));

    expect(screen.getByRole("heading", { name: "Tickets page" })).toBeInTheDocument();
  });
});
