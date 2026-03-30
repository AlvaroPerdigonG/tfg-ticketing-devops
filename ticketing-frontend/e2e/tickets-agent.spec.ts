import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";

test("TICKET-AGENT-03 agente ve tickets gestionables", async ({ page }) => {
  await loginAs(page, "agent");

  await expect(page).toHaveURL(/\/tickets$/);
  await expect(page.getByTestId("agent-tickets-title")).toBeVisible();
  await expect(page.getByRole("button", { name: "Sin asignar" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Asignados a mí" })).toBeVisible();
});
