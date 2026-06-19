import { defineConfig, devices } from "@playwright/test";
import path from "path";

const DIST_DIR = path.resolve(
  __dirname,
  "../webApp/build/dist/wasmJs/productionExecutable"
);

export default defineConfig({
  testDir: "./tests",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI
    ? [["junit", { outputFile: "test-results/results.xml" }], ["list"]]
    : "list",

  // Snapshot path for visual regression screenshots (checked into git)
  snapshotPathTemplate: "{testDir}/__snapshots__/{testName}/{arg}{ext}",

  use: {
    baseURL: "http://localhost:3000",
    // The webApp renders entirely to <canvas> — trace captures clicks and
    // screenshots so failures can be inspected in the Playwright HTML report.
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    // Compose Multiplatform WasmJs needs WebAssembly; Chromium supports it.
    // Disable web security only for local testing where the wasm origin matters.
    launchOptions: {
      args: ["--enable-features=WebAssembly"],
    },
  },

  // Serve the pre-built webApp distribution (run `./gradlew :webApp:wasmJsBrowserDistribution` first)
  webServer: {
    command: `npx serve "${DIST_DIR}" --listen 3000 --no-clipboard`,
    url: "http://localhost:3000",
    reuseExistingServer: !process.env.CI,
    timeout: 30_000,
    stdout: "ignore",
    stderr: "pipe",
  },

  projects: [
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        viewport: { width: 1280, height: 800 },
      },
    },
  ],
});
