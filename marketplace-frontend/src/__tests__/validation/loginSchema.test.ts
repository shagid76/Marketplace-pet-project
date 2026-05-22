import { loginSchema } from "../../validation/loginSchema";

describe("loginSchema", () => {
    const valid = { email: "user@example.com", password: "password123" };

    it("passes with valid email and password", () => {
        expect(loginSchema.safeParse(valid).success).toBe(true);
    });

    // email
    it("fails when email is missing", () => {
        const result = loginSchema.safeParse({ ...valid, email: "" });
        expect(result.success).toBe(false);
    });

    it("fails when email has no @ sign", () => {
        const result = loginSchema.safeParse({ ...valid, email: "notanemail" });
        expect(result.success).toBe(false);
    });

    it("fails when email is too short (under 5 chars)", () => {
        const result = loginSchema.safeParse({ ...valid, email: "a@b." });
        expect(result.success).toBe(false);
    });

    it("fails when email exceeds 128 characters", () => {
        const long = "a".repeat(120) + "@test.com";
        const result = loginSchema.safeParse({ ...valid, email: long });
        expect(result.success).toBe(false);
    });

    it("trims whitespace from email before validation", () => {
        const result = loginSchema.safeParse({ ...valid, email: "  user@example.com  " });
        expect(result.success).toBe(true);
    });

    // password
    it("fails when password is too short (under 8 chars)", () => {
        const result = loginSchema.safeParse({ ...valid, password: "short" });
        expect(result.success).toBe(false);
    });

    it("passes when password is exactly 8 characters", () => {
        const result = loginSchema.safeParse({ ...valid, password: "exactly8" });
        expect(result.success).toBe(true);
    });

    it("fails when password exceeds 128 characters", () => {
        const result = loginSchema.safeParse({ ...valid, password: "a".repeat(129) });
        expect(result.success).toBe(false);
    });

    it("fails when password is missing", () => {
        const result = loginSchema.safeParse({ email: "user@example.com" });
        expect(result.success).toBe(false);
    });
});
