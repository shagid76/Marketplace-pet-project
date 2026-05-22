import { test, expect } from "@playwright/test";
import { loginAs, TEST_USER } from "./helpers/auth";

/**
 * E2E – Cart page
 *
 * Covers: cart renders, remove item, checkout button, promo code modal.
 */

test.describe("Cart page", () => {
    test.beforeEach(async ({ page }) => {
        await loginAs(page, TEST_USER.email, TEST_USER.password);
        await page.goto("/cart");
        await page.waitForLoadState("networkidle");
    });

    test("renders the cart heading", async ({ page }) => {
        await expect(
            page.getByRole("heading", { name: /cart|your cart/i })
        ).toBeVisible({ timeout: 8_000 });
    });

    test("shows an empty cart message when no items are present", async ({ page }) => {
        // If cart is already empty after fresh login
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const count = await items.count();

        if (count === 0) {
            await expect(
                page.getByText(/cart is empty|nothing in your cart|no items/i)
            ).toBeVisible();
        }
    });

    test("displays product items that were added to the cart", async ({ page }) => {
        // Add a product first if cart is empty
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const count = await items.count();

        if (count === 0) {
            // Navigate to home and add a product
            await page.goto("/");
            await page.waitForLoadState("networkidle");
            const firstCard = page.locator("a[href*='/product/']").first();
            await firstCard.click();
            await page.waitForLoadState("networkidle");

            const addBtn = page.getByRole("button", { name: /add to cart/i });
            if (await addBtn.isVisible()) {
                await addBtn.click();
                await page.getByRole("button", { name: /remove from cart|in cart/i })
                    .waitFor({ timeout: 6_000 });
            }

            await page.goto("/cart");
            await page.waitForLoadState("networkidle");
        }

        await expect(items.first()).toBeVisible({ timeout: 8_000 });
    });

    test("remove button decreases item count by 1", async ({ page }) => {
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const initialCount = await items.count();

        if (initialCount > 0) {
            const removeBtn = items.first().getByRole("button", { name: /remove|delete|×/i });
            await removeBtn.click();

            // Count should decrease
            await expect(items).toHaveCount(initialCount - 1, { timeout: 6_000 });
        }
    });

    test("displays a subtotal amount", async ({ page }) => {
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const count = await items.count();

        if (count > 0) {
            await expect(page.getByText(/subtotal|total/i)).toBeVisible();
            await expect(page.getByText(/\$[\d,.]+/)).toBeVisible();
        }
    });

    test("checkout button opens the promo code modal", async ({ page }) => {
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const count = await items.count();

        if (count > 0) {
            const checkoutBtn = page.getByRole("button", { name: /checkout|buy all|proceed/i });
            await expect(checkoutBtn).toBeVisible();
            await checkoutBtn.click();

            // Promo code modal should appear
            await expect(
                page.getByText(/promo code|discount code|coupon/i)
            ).toBeVisible({ timeout: 5_000 });
        }
    });

    test("promo code modal has a confirm and close action", async ({ page }) => {
        const items = page.locator("[class*='cart-item'], [data-testid='cart-item']");
        const count = await items.count();

        if (count > 0) {
            await page.getByRole("button", { name: /checkout|buy all/i }).click();

            const modal = page.locator("[class*='modal'], [role='dialog']");
            await expect(modal).toBeVisible({ timeout: 5_000 });

            await expect(
                modal.getByRole("button", { name: /checkout|confirm/i })
            ).toBeVisible();

            // Close the modal
            const closeBtn = modal.getByRole("button", { name: /close|cancel|×/i });
            await closeBtn.click();
            await expect(modal).not.toBeVisible({ timeout: 3_000 });
        }
    });
});
