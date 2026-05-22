import { test, expect } from "@playwright/test";

/**
 * E2E – Home page
 *
 * Covers: new items section, category tabs, category product listing.
 */

test.describe("Home page", () => {
    test.beforeEach(async ({ page }) => {
        await page.goto("/");
    });

    test("renders the page without crashing", async ({ page }) => {
        await expect(page).toHaveTitle(/.+/);
        // Header and footer are always present
        await expect(page.locator("header")).toBeVisible();
        await expect(page.locator("footer")).toBeVisible();
    });

    test("displays the new items section with at least one product card", async ({ page }) => {
        // Allow time for the API to respond
        const cards = page.locator("[class*='product-card'], [data-testid='product-card']");
        await expect(cards.first()).toBeVisible({ timeout: 10_000 });
    });

    test("shows category selector buttons", async ({ page }) => {
        const categoryBtns = page.getByRole("button", {
            name: /electronics|fashion|sports|motors|music/i,
        });
        await expect(categoryBtns.first()).toBeVisible({ timeout: 8_000 });
    });

    test("switching category updates the product listing", async ({ page }) => {
        // Wait for initial content
        await page.waitForLoadState("networkidle");

        const electronicsBtn = page.getByRole("button", { name: /electronics/i });
        const fashionBtn = page.getByRole("button", { name: /fashion/i });

        // Click Electronics first (default) — note initial text
        await electronicsBtn.click();
        await page.waitForLoadState("networkidle");
        const electronicsContent = await page.locator("main").textContent();

        // Now switch to Fashion
        await fashionBtn.click();
        await page.waitForLoadState("networkidle");
        const fashionContent = await page.locator("main").textContent();

        // Content must have changed
        expect(fashionContent).not.toBe(electronicsContent);
    });

    test("product card links navigate to the product detail page", async ({ page }) => {
        await page.waitForLoadState("networkidle");

        const firstCard = page
            .locator("a[href*='/product/']")
            .first();
        await expect(firstCard).toBeVisible({ timeout: 10_000 });

        const href = await firstCard.getAttribute("href");
        await firstCard.click();
        await expect(page).toHaveURL(new RegExp(href!));
    });
});
