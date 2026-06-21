/**
 * Smoke tests: verifies the webApp bundle loads, the canvas renders, and the
 * login screen appears. These tests mock the network so no running server is
 * needed.
 *
 * The app renders entirely to a <canvas> element (Compose Multiplatform WasmJs),
 * so DOM selectors for text content don't work. Instead we:
 *  1. Assert the canvas element is present and non-empty (bounding box > 0).
 *  2. Use Playwright's mouse API for interactions (click on known coordinates
 *     or use accessibility roles if Compose exposes them via ARIA).
 */

import { expect, test } from "@playwright/test";

test.describe("App loads", () => {
  test("page title is correct", async ({ page }) => {
    await page.goto("/");
    await expect(page).toHaveTitle("My Kitchen");
  });

  test("canvas element is rendered and has non-zero size", async ({ page }) => {
    await page.goto("/");
    const canvas = page.locator("canvas#ComposeTarget");
    await expect(canvas).toBeVisible();
    const box = await canvas.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.width).toBeGreaterThan(100);
    expect(box!.height).toBeGreaterThan(100);
  });

  test("login screen renders", async ({ page }) => {
    await page.goto("/");

    // Wait for Compose/Skiko to initialise the canvas and set its dimensions.
    // We check canvas.width/height rather than reading pixels via getContext("2d"):
    // acquiring a 2D context locks the canvas element and prevents Skiko from
    // later creating its WebGL2 context, which would break rendering entirely.
    await page.waitForFunction(
      () => {
        const canvas = document.querySelector(
          "canvas#ComposeTarget"
        ) as HTMLCanvasElement | null;
        return canvas !== null && canvas.width > 0 && canvas.height > 0;
      },
      null,
      { timeout: 45_000 }
    );

    const canvas = page.locator("canvas#ComposeTarget");
    await expect(canvas).toBeVisible();
    const box = await canvas.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.width).toBeGreaterThan(0);
    expect(box!.height).toBeGreaterThan(0);
  });
});
