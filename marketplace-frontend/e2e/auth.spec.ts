import { test, expect } from "@playwright/test";
import { TEST_USER, loginAs, logout } from "./helpers/auth";

/**
 * E2E – Authentication flows
 *
 * Covers: login, registration, protected-route redirect, logout.
 * These tests run against a live dev server + backend.
 */

test.describe("Login", () => {
    test.beforeEach(async ({ page }) => {
        await page.goto("/login");
    });

    test("renders the login form", async ({ page }) => {
        await expect(page.getByLabel(/email/i)).toBeVisible();
        await expect(page.getByLabel(/password/i)).toBeVisible();
        await expect(page.getByRole("button", { name: /sign in/i })).toBeVisible();
    });

    test("shows validation errors on empty submit", async ({ page }) => {
        await page.getByRole("button", { name: /sign in/i }).click();
        await expect(page.getByText(/email is required|invalid email/i)).toBeVisible();
    });

    test("shows an error for invalid credentials", async ({ page }) => {
        await page.getByLabel(/email/i).fill("nobody@example.com");
        await page.getByLabel(/password/i).fill("WrongPass999!");
        await page.getByRole("button", { name: /sign in/i }).click();
        await expect(
            page.getByText(/invalid credentials|incorrect|not found/i)
        ).toBeVisible({ timeout: 8000 });
    });

    test("redirects to home on successful login", async ({ page }) => {
        await loginAs(page, TEST_USER.email, TEST_USER.password);
        await expect(page).toHaveURL("/");
    });

    test("shows the username in the header after login", async ({ page }) => {
        await loginAs(page, TEST_USER.email, TEST_USER.password);
        await expect(page.getByText(TEST_USER.username, { exact: false })).toBeVisible();
    });
});

test.describe("Registration", () => {
    test("renders the registration form", async ({ page }) => {
        await page.goto("/registration");
        await expect(page.getByLabel(/username/i)).toBeVisible();
        await expect(page.getByLabel(/email/i)).toBeVisible();
        await expect(page.getByLabel(/password/i).first()).toBeVisible();
    });

    test("shows validation errors for mismatched passwords", async ({ page }) => {
        await page.goto("/registration");
        await page.getByLabel(/username/i).fill("newuser");
        await page.getByLabel(/email/i).fill("new@example.com");
        const passwordFields = page.getByLabel(/password/i);
        await passwordFields.first().fill("Password1!");
        await passwordFields.nth(1).fill("DifferentPass1!");
        await page.getByRole("button", { name: /register|sign up/i }).click();
        await expect(page.getByText(/passwords? do not match|must match/i)).toBeVisible();
    });
});

test.describe("Protected routes", () => {
    test("redirects unauthenticated users to /login when accessing /cart", async ({ page }) => {
        await page.goto("/cart");
        await expect(page).toHaveURL(/\/login/);
    });

    test("redirects unauthenticated users to /login when accessing /me", async ({ page }) => {
        await page.goto("/me");
        await expect(page).toHaveURL(/\/login/);
    });

    test("redirects unauthenticated users to /login when accessing /create-product", async ({ page }) => {
        await page.goto("/create-product");
        await expect(page).toHaveURL(/\/login/);
    });
});

test.describe("Logout", () => {
    test.beforeEach(async ({ page }) => {
        await loginAs(page, TEST_USER.email, TEST_USER.password);
    });

    test("redirects to /login after logging out", async ({ page }) => {
        await logout(page);
        await expect(page).toHaveURL("/login");
    });

    test("clears access token from localStorage on logout", async ({ page }) => {
        await logout(page);
        const token = await page.evaluate(() => localStorage.getItem("accessToken"));
        expect(token).toBeNull();
    });

    test("protected pages are inaccessible after logout", async ({ page }) => {
        await logout(page);
        await page.goto("/me");
        await expect(page).toHaveURL(/\/login/);
    });
});
