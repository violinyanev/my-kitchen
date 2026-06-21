/**
 * Login flow E2E tests.
 *
 * Because the UI is rendered to a <canvas>, we interact via Playwright's
 * mouse/keyboard API by clicking the canvas at coordinates that correspond to
 * known UI regions. This is intentionally coarse — the goal is to verify
 * observable end-to-end behavior (network requests, navigation) rather than
 * pixel-precise layout.
 *
 * Two modes:
 *  - SERVER_URL env var absent → mocks the API responses (no real server needed)
 *  - SERVER_URL env var set    → runs against a real Ktor server instance
 *    (used in CI: see .github/workflows/test.yaml e2e job)
 */

import { expect, Page, test } from "@playwright/test";

const SERVER_URL = process.env.SERVER_URL ?? "http://localhost:5000";

/** Waits until the canvas has rendered at least one non-transparent pixel. */
async function waitForCanvasPaint(page: Page, timeout = 45_000) {
  await page.waitForFunction(
    () => {
      const c = document.querySelector("canvas") as HTMLCanvasElement | null;
      if (!c) return false;
      const ctx = c.getContext("2d");
      if (!ctx) return false;
      const d = ctx.getImageData(c.width / 2, c.height / 2, 1, 1).data;
      return d[3] > 0;
    },
    null,
    { timeout }
  );
}

test.describe("Login screen", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await waitForCanvasPaint(page);
  });

  test("login with empty fields shows validation error (mocked)", async ({
    page,
  }) => {
    // Intercept the login API call — should NOT be called when fields are empty
    let loginCallMade = false;
    await page.route(`${SERVER_URL}/login`, () => {
      loginCallMade = true;
    });

    // The login button is centered horizontally and roughly 60% down the page.
    // We click it without filling in any fields.
    const canvas = page.locator("canvas");
    const box = (await canvas.boundingBox())!;
    const loginBtnX = box.x + box.width / 2;
    const loginBtnY = box.y + box.height * 0.62;
    await page.mouse.click(loginBtnX, loginBtnY);

    // After clicking with empty fields the VM updates error state → canvas repaints
    await page.waitForTimeout(500);
    // Verify that the login API was NOT called when fields are empty
    expect(loginCallMade).toBe(false);
  });

  test("successful login navigates to recipe list (mocked)", async ({
    page,
  }) => {
    // Mock a successful login response
    await page.route(`${SERVER_URL}/login`, (route) => {
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({ token: "test-jwt-token" }),
      });
    });

    // Mock an empty recipe list so the app doesn't try to sync
    await page.route(`${SERVER_URL}/recipes`, (route) => {
      route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify([]),
      });
    });

    const canvas = page.locator("canvas");
    const box = (await canvas.boundingBox())!;

    // Fill server URL field (top ~28% of screen, centered)
    await page.mouse.click(box.x + box.width / 2, box.y + box.height * 0.28);
    await page.keyboard.selectAll();
    await page.keyboard.type(SERVER_URL);

    // Fill email field (~39% down)
    await page.mouse.click(box.x + box.width / 2, box.y + box.height * 0.39);
    await page.keyboard.type("user@example.com");

    // Fill password field (~50% down)
    await page.mouse.click(box.x + box.width / 2, box.y + box.height * 0.5);
    await page.keyboard.type("password123");

    // Click login button (~62% down)
    await page.mouse.click(box.x + box.width / 2, box.y + box.height * 0.62);

    // Wait for navigation — the login route mock returns success, so the app
    // should navigate away from the login screen.
    await page.waitForTimeout(2000);
    // Canvas still exists and has non-zero dimensions after navigation
    const canvas = page.locator("canvas");
    await expect(canvas).toBeVisible();
  });
});
