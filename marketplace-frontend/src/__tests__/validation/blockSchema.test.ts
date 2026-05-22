import { blockSchema } from "../../validation/blockSchema";

describe("blockSchema", () => {
    const futureDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000); // one week from now

    const valid = {
        targetId: "user-42",
        reason: "Repeated spam behavior",
        bannedUntil: futureDate.toISOString(),
    };

    it("passes with valid data and a future date", () => {
        expect(blockSchema.safeParse(valid).success).toBe(true);
    });

    it("fails when targetId is empty", () => {
        const result = blockSchema.safeParse({ ...valid, targetId: "" });
        expect(result.success).toBe(false);
    });

    it("fails when reason is too short (under 5 chars)", () => {
        const result = blockSchema.safeParse({ ...valid, reason: "bad" });
        expect(result.success).toBe(false);
    });

    it("passes when reason is exactly 5 characters", () => {
        const result = blockSchema.safeParse({ ...valid, reason: "okkkk" });
        expect(result.success).toBe(true);
    });

    it("fails when reason exceeds 128 characters", () => {
        const result = blockSchema.safeParse({ ...valid, reason: "a".repeat(129) });
        expect(result.success).toBe(false);
    });

    it("fails when bannedUntil is in the past", () => {
        const past = new Date(Date.now() - 60 * 1000).toISOString();
        const result = blockSchema.safeParse({ ...valid, bannedUntil: past });
        expect(result.success).toBe(false);
    });

    it("fails when bannedUntil is missing", () => {
        const { bannedUntil, ...rest } = valid;
        const result = blockSchema.safeParse(rest);
        expect(result.success).toBe(false);
    });

    it("fails when bannedUntil is not a valid date string", () => {
        const result = blockSchema.safeParse({ ...valid, bannedUntil: "not-a-date" });
        expect(result.success).toBe(false);
    });

    it("trims whitespace from reason before validation", () => {
        const result = blockSchema.safeParse({ ...valid, reason: "  Valid reason here  " });
        expect(result.success).toBe(true);
    });
});
