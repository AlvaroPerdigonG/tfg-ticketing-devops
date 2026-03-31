import { expect, type Page } from "@playwright/test";

export async function selectFirstTicketCategory(page: Page) {
  const categorySelect = page.getByTestId("create-ticket-category");
  await expect(categorySelect).toBeVisible();
  await expect(categorySelect).not.toHaveClass(/ant-select-disabled/);

  await categorySelect.click();

  const firstCategoryOption = page.locator(".ant-select-dropdown .ant-select-item-option").first();
  await expect(firstCategoryOption).toBeVisible();
  await firstCategoryOption.click();
}
