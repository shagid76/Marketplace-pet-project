import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for the marketplace frontend.
 *
 * Run all specs:          npx playwright test
 * Run with UI:            npx playwright test --ui
 * Show last HTML report:  npx playwright show-report
 *
 * The frontend dev server is started automatically before tests and torn down
 * afterwards. Set PLAYWRIGHT_BASE_URL to target a deployed environment instead.
 */
export default defineConfig({
    testDir: "./e2e",
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    workers: process.env.CI ? 1 : undefined,

    reporter: [["html", { open: "never" }], ["list"]],

    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL ?? "http://localhost:3000",
        trace: "on-first-retry",
        screenshot: "only-on-failure",
        video: "retain-on-failure",
    },

    projects: [
        {
            name: "chromium",
            use: { ...devices["Desktop Chrome"] },
        },
        {
            name: "firefox",
            use: { ...devices["Desktop Firefox"] },
        },
        {
            name: "mobile-chrome",
            use: { ...devices["Pixel 5"] },
        },
    ],

    /* Start the CRA dev server locally before the test run */
    webServer: {
        command: "npm start",
        url: "http://localhost:3000",
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
    },
});
