import type { Page } from "@playwright/test";
import { expect } from "@playwright/test";
import { getCredentials } from "../utils/env";

type Role = "user" | "agent" | "admin";

export async function loginAs(page: Page, role: Role) {
  const credentials = getCredentials(role);

  await page.goto("/login");

  await page.getByTestId("auth-email").fill(credentials.email);
  await page.getByTestId("auth-password").fill(credentials.password);
  await page.getByTestId("auth-submit").click();

  await expect(page).not.toHaveURL(/\/login$/);
  await expect(page.getByRole("button", { name: "Logout" })).toBeVisible();
}
