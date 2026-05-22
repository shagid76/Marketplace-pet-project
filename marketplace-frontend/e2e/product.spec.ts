import { test, expect } from "@playwright/test";
import { loginAs, TEST_USER } from "./helpers/auth";

/**
 * E2E – Product detail page
 *
 * Covers: page renders, add-to-cart, add-to-wishlist, unauthenticated guards.
 */

/** Navigate to the first available product detail page from the home page. */
async function gotoFirstProduct(page: import("@playwright/test").Page) {
    await page.goto("/");
    await page.waitForLoadState("networkidle");
    const firstCard = page.locator("a[href*='/product/']").first();
    await expect(firstCard).toBeVisible({ timeout: 10_000 });
    await firstCard.click();
    await page.waitForLoadState("networkidle");
}

test.describe("Product detail page", () => {
    test("displays product title, price and description", async ({ page }) => {
        await gotoFirstProduct(page);

        await expect(page.getByRole("heading").first()).toBeVisible();
        // Price text contains a currency symbol or numeric value
        await expect(page.getByText(/\$[\d,]+|[\d,]+\s*(USD|€)/)).toBeVisible();
    });

    test("displays product images", async ({ page }) => {
        await gotoFirstProduct(page);
        const images = page.locator("img").filter({ hasNotText: "" });
        await expect(images.first()).toBeVisible({ timeout: 8_000 });
    });

    test("shows the seller / author section", async ({ page }) => {
        await gotoFirstProduct(page);
        // Author section typically contains a link to the user profile
        await expect(page.locator("a[href*='/user/']")).toBeVisible({ timeout: 8_000 });
    });

    test.describe("Unauthenticated user", () => {
        test("clicking Add to cart redirects to /login", async ({ page }) => {
            await gotoFirstProduct(page);

            const addToCartBtn = page.getByRole("button", { name: /add to cart/i });
            await expect(addToCartBtn).toBeVisible({ timeout: 8_000 });
            await addToCartBtn.click();

            await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
        });

        test("clicking Add to wishlist redirects to /login", async ({ page }) => {
            await gotoFirstProduct(page);

            const wishlistBtn = page.getByRole("button", { name: /wishlist|save/i });
            if (await wishlistBtn.isVisible()) {
                await wishlistBtn.click();
                await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
            }
        });
    });

    test.describe("Authenticated user", () => {
        test.beforeEach(async ({ page }) => {
            await loginAs(page, TEST_USER.email, TEST_USER.password);
        });

        test("can add a product to cart", async ({ page }) => {
            await gotoFirstProduct(page);

            const addToCartBtn = page.getByRole("button", { name: /add to cart/i });
            await expect(addToCartBtn).toBeVisible({ timeout: 8_000 });
            await addToCartBtn.click();

            // Button label changes to "Remove from cart" or a success state
            await expect(
                page.getByRole("button", { name: /remove from cart|in cart/i })
            ).toBeVisible({ timeout: 6_000 });
        });

        test("can add a product to wishlist", async ({ page }) => {
            await gotoFirstProduct(page);

            const wishlistBtn = page.getByRole("button", { name: /add to wishlist|save/i });
            if (await wishlistBtn.isVisible()) {
                await wishlistBtn.click();
                await expect(
                    page.getByRole("button", { name: /remove from wishlist|saved/i })
                ).toBeVisible({ timeout: 6_000 });
            }
        });
    });
});
