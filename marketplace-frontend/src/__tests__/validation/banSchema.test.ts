import { banSchema } from "../../validation/banSchema";

describe("banSchema", () => {
    const valid = {
        targetType: "USER",
        targetId: "abc123",
        reason: "Violated terms of service",
    };

    it("passes with valid USER target", () => {
        expect(banSchema.safeParse(valid).success).toBe(true);
    });

    it("passes with PRODUCT targetType", () => {
        const result = banSchema.safeParse({ ...valid, targetType: "PRODUCT" });
        expect(result.success).toBe(true);
    });

    it("passes with REVIEW targetType", () => {
        const result = banSchema.safeParse({ ...valid, targetType: "REVIEW" });
        expect(result.success).toBe(true);
    });

    it("fails with invalid targetType", () => {
        const result = banSchema.safeParse({ ...valid, targetType: "ORDER" });
        expect(result.success).toBe(false);
    });

    it("fails when targetId is empty", () => {
        const result = banSchema.safeParse({ ...valid, targetId: "" });
        expect(result.success).toBe(false);
    });

    it("fails when reason is too short (under 5 chars)", () => {
        const result = banSchema.safeParse({ ...valid, reason: "bad" });
        expect(result.success).toBe(false);
    });

    it("passes when reason is exactly 5 characters", () => {
        const result = banSchema.safeParse({ ...valid, reason: "12345" });
        expect(result.success).toBe(true);
    });

    it("fails when reason exceeds 128 characters", () => {
        const result = banSchema.safeParse({ ...valid, reason: "a".repeat(129) });
        expect(result.success).toBe(false);
    });

    it("trims whitespace from reason", () => {
        const result = banSchema.safeParse({ ...valid, reason: "  Valid reason  " });
        expect(result.success).toBe(true);
    });

    it("fails when reason is empty after trimming", () => {
        const result = banSchema.safeParse({ ...valid, reason: "    " });
        expect(result.success).toBe(false);
    });
});
