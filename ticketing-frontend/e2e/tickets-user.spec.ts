import { expect, test } from "@playwright/test";
import { loginAs } from "./fixtures/auth";
import { selectFirstTicketCategory } from "./fixtures/tickets";

test("TICKET-USER-01 User creates ticket correctly", async ({ page }) => {
  await loginAs(page, "user");
  await page.getByTestId("user-create-ticket-cta").click();

  await expect(page).toHaveURL(/\/tickets\/new$/);

  const uniqueSuffix = Date.now();
  await page.getByLabel("Title").fill(`E2E ticket ${uniqueSuffix}`);
  await page.getByLabel("Description").fill("Minimal E2E flow to validate ticket creation.");

  await selectFirstTicketCategory(page);
  await page.getByTestId("create-ticket-submit").click();

  await expect(page).toHaveURL(/\/tickets\/[0-9a-f-]+$/i);
  await expect(page.getByText(`E2E ticket ${uniqueSuffix}`)).toBeVisible();
});
