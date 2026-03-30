import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";
import { hasAdminCredentials } from "./utils/env";

test("ADMIN-01 admin puede listar usuarios", async ({ page }) => {
  test.skip(!hasAdminCredentials(), "Define E2E_ADMIN_EMAIL y E2E_ADMIN_PASSWORD para ejecutar ADMIN-01.");

  await loginAs(page, "admin");
  await page.goto("/admin");

  await expect(page.getByTestId("admin-title")).toBeVisible();
  await page.getByRole("tab", { name: "Usuarios" }).click();
  await expect(page.getByText("Email")).toBeVisible();
});
