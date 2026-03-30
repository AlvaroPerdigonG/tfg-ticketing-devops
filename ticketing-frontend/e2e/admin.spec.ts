import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";

test("ADMIN-04 usuario no autorizado no accede a administración", async ({ page }) => {
  await loginAs(page, "user");
  await page.goto("/admin");

  await expect(page).toHaveURL(/\/forbidden$/);
  await expect(page.getByText("403 — Forbidden")).toBeVisible();
});
