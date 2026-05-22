import { Page } from "@playwright/test";

/**
 * Shared helpers for E2E auth flows.
 * Credentials match the seeded test users expected to exist in the dev environment.
 */

export const TEST_USER = {
    email: "testuser@example.com",
    password: "Test1234!",
    username: "testuser",
};

export const TEST_ADMIN = {
    email: "admin@example.com",
    password: "Admin1234!",
    username: "admin",
};

/** Fill in and submit the login form, then wait for the home page to load. */
export async function loginAs(page: Page, email: string, password: string) {
    await page.goto("/login");
    await page.getByLabel(/email/i).fill(email);
    await page.getByLabel(/password/i).fill(password);
    await page.getByRole("button", { name: /sign in/i }).click();
    await page.waitForURL("/");
}

/** Log out via the UI (header button or profile menu). */
export async function logout(page: Page) {
    await page.getByRole("button", { name: /log ?out|sign ?out/i }).click();
    await page.waitForURL("/login");
}

/** Inject tokens directly into localStorage — faster than going through the UI. */
export async function injectTokens(
    page: Page,
    accessToken: string,
    refreshToken: string
) {
    await page.addInitScript(
        ({ access, refresh }) => {
            localStorage.setItem("accessToken", access);
            localStorage.setItem("refreshToken", refresh);
        },
        { access: accessToken, refresh: refreshToken }
    );
}
