import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";
import { getCredentials } from "./utils/env";

test("AUTH-01 login correcto redirige al área autenticada", async ({ page }) => {
  await loginAs(page, "user");

  await expect(page).toHaveURL(/\/tickets$/);
  await expect(page.getByTestId("user-tickets-title")).toBeVisible();
});

test("AUTH-02 login incorrecto muestra error y mantiene sesión no autenticada", async ({
  page,
}) => {
  const user = getCredentials("user");

  await page.goto("/login");
  await page.getByTestId("auth-email").fill(user.email);
  await page.getByTestId("auth-password").fill(`${user.password}-wrong`);
  await page.getByTestId("auth-submit").click();

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByTestId("auth-error")).toBeVisible();
  await expect(page.getByRole("button", { name: "Logout" })).toHaveCount(0);
});
