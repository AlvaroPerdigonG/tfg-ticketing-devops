import { expect, test, type Page } from "@playwright/test";
import { loginAs } from "./fixtures/auth";
import { selectFirstTicketCategory } from "./fixtures/tickets";

async function createTicketAsUser(page: Page) {
  const uniqueSuffix = Date.now();
  const title = `E2E agente ${uniqueSuffix}`;

  await loginAs(page, "user");
  await page.getByTestId("user-create-ticket-cta").click();
  await expect(page).toHaveURL(/\/tickets\/new$/);

  await page.getByLabel("Title").fill(title);
  await page
    .getByLabel("Description")
    .fill("Ticket creado para validar cambio de estado por agente.");
  await selectFirstTicketCategory(page);
  await page.getByTestId("create-ticket-submit").click();

  await expect(page).toHaveURL(/\/tickets\/[0-9a-f-]+$/i);
  await expect(page.getByTestId("ticket-detail-title")).toContainText(title);
  return title;
}

test("TICKET-AGENT-01 agent/admin cambia estado correctamente", async ({ page }) => {
  const ticketTitle = await createTicketAsUser(page);

  await page.getByRole("button", { name: "Logout" }).click();
  await loginAs(page, "agent");
  await page.goto("/tickets");
  await expect(page).toHaveURL(/\/tickets(?:\?.*)?$/);
  await page.getByRole("button", { name: "All" }).click();

  await page.getByPlaceholder("Search by title or ID").fill(ticketTitle);
  await page.getByRole("button", { name: "Search" }).click();
  await page.getByRole("button", { name: "View" }).first().click();

  await expect(page.getByTestId("ticket-detail-title")).toContainText(ticketTitle);
  await expect(page.getByTestId("ticket-detail-status")).toContainText("Open");

  await page.getByTestId("ticket-status-transition-IN_PROGRESS").click();
  await expect(page.getByTestId("ticket-detail-status")).toContainText("In progress");
});

test("TICKET-AGENT-04 agent se asigna ticket y comenta en el detalle", async ({ page }) => {
  const ticketTitle = await createTicketAsUser(page);

  await page.getByRole("button", { name: "Logout" }).click();
  await loginAs(page, "agent");
  await page.goto("/tickets");
  await expect(page).toHaveURL(/\/tickets(?:\?.*)?$/);
  await page.getByRole("button", { name: "All" }).click();

  await page.getByPlaceholder("Search by title or ID").fill(ticketTitle);
  await page.getByRole("button", { name: "Search" }).click();
  await page.getByRole("button", { name: "View" }).first().click();

  await expect(page.getByTestId("ticket-detail-title")).toContainText(ticketTitle);

  await page.getByRole("button", { name: "Assign ticket to me" }).click();
  await expect(page.getByRole("button", { name: "Assign ticket to me" })).toHaveCount(0);

  const commentText = `Comentario E2E agente ${Date.now()}`;
  await page.getByPlaceholder("Write a comment").fill(commentText);
  await page.getByRole("button", { name: "Send comment" }).click();

  await expect(page.getByText(commentText)).toBeVisible();
});
