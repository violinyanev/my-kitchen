/**
 * Smoke tests: verifies the webApp bundle loads, the canvas renders, and the
 * login screen appears. These tests mock the network so no running server is
 * needed.
 *
 * The app renders entirely to a <canvas> element (Compose Multiplatform WasmJs),
 * so DOM selectors for text content don't work. Instead we:
 *  1. Assert the canvas element is present and non-empty (bounding box > 0).
 *  2. Use screenshot comparison for visual regression.
 *  3. Use Playwright's mouse API for interactions (click on known coordinates
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

  test("login screen renders (visual snapshot)", async ({ page }) => {
    await page.goto("/");

    // Wait for the Compose/WasmJs runtime to paint something on the canvas.
    // The canvas starts blank; we wait until at least one non-transparent pixel
    // exists (checked via JS canvas API) to know the UI has rendered.
    await page.waitForFunction(
      () => {
        const canvas = document.querySelector(
          "canvas#ComposeTarget"
        ) as HTMLCanvasElement;
        if (!canvas) return false;
        const ctx = canvas.getContext("2d");
        if (!ctx) return false;
        const data = ctx.getImageData(
          canvas.width / 2,
          canvas.height / 2,
          1,
          1
        ).data;
        // Alpha > 0 means something was drawn
        return data[3] > 0;
      },
      null,
      { timeout: 15_000 }
    );

    await expect(page).toHaveScreenshot("login-screen.png", {
      maxDiffPixels: 200, // allow minor anti-aliasing differences across runs
    });
  });
});
