import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";

test("AUTH-01 login correcto redirige al área autenticada", async ({ page }) => {
  await loginAs(page, "user");

  await expect(page).toHaveURL(/\/tickets$/);
  await expect(page.getByTestId("user-tickets-title")).toBeVisible();
});
