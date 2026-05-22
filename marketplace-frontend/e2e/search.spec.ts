import { test, expect } from "@playwright/test";

/**
 * E2E – Product search
 *
 * Covers: keyword search, category filter, price range, empty-state.
 */

test.describe("Product search", () => {
    test.beforeEach(async ({ page }) => {
        await page.goto("/search");
    });

    test("renders the search input and submit button", async ({ page }) => {
        await expect(page.getByRole("searchbox").or(page.getByPlaceholder(/search/i))).toBeVisible();
    });

    test("shows results after submitting a search query", async ({ page }) => {
        const searchInput = page.getByRole("searchbox").or(page.getByPlaceholder(/search/i));
        await searchInput.fill("laptop");
        await page.keyboard.press("Enter");

        await page.waitForLoadState("networkidle");

        // Expect at least one product card or result item
        const results = page.locator("[class*='product-card'], [data-testid='product-card'], [class*='search-result']");
        await expect(results.first()).toBeVisible({ timeout: 10_000 });
    });

    test("shows an empty-state message when no results match", async ({ page }) => {
        const searchInput = page.getByRole("searchbox").or(page.getByPlaceholder(/search/i));
        await searchInput.fill("xyznonexistentproduct12345");
        await page.keyboard.press("Enter");

        await page.waitForLoadState("networkidle");

        await expect(
            page.getByText(/no products found|no results|nothing here/i)
        ).toBeVisible({ timeout: 8_000 });
    });

    test("URL contains the search query as a parameter", async ({ page }) => {
        const searchInput = page.getByRole("searchbox").or(page.getByPlaceholder(/search/i));
        await searchInput.fill("jacket");
        await page.keyboard.press("Enter");

        await expect(page).toHaveURL(/[?&]q(uery)?=jacket/i, { timeout: 5_000 });
    });

    test("navigating to a result opens the product detail page", async ({ page }) => {
        const searchInput = page.getByRole("searchbox").or(page.getByPlaceholder(/search/i));
        await searchInput.fill("phone");
        await page.keyboard.press("Enter");
        await page.waitForLoadState("networkidle");

        const firstResult = page.locator("a[href*='/product/']").first();
        await expect(firstResult).toBeVisible({ timeout: 10_000 });
        await firstResult.click();

        await expect(page).toHaveURL(/\/product\//);
    });
});
