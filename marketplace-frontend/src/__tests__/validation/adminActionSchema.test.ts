import { adminActionSchema } from "../../validation/adminActionSchema";

const validBan = {
    targetId: "user-abc-123",
    targetType: "USER" as const,
    actionType: "BAN" as const,
    reason: "Violated terms of service repeatedly",
};

const validBlock = {
    targetId: "user-abc-123",
    targetType: "USER" as const,
    actionType: "BLOCK" as const,
    reason: "Suspicious activity detected",
    expiresAt: "2030-01-01T00:00:00",
};

// BAN rules
describe("adminActionSchema - BAN", () => {
    it("accepts a valid BAN without expiresAt", () => {
        const result = adminActionSchema.safeParse(validBan);
        expect(result.success).toBe(true);
    });

    it("rejects BAN when expiresAt is provided (ban is permanent)", () => {
        const result = adminActionSchema.safeParse({
            ...validBan,
            expiresAt: "2030-01-01T00:00:00",
        });
        expect(result.success).toBe(false);
        if (!result.success) {
            const paths = result.error.issues.map((i) => i.path[0]);
            expect(paths).toContain("expiresAt");
        }
    });
});

// BLOCK rules
describe("adminActionSchema - BLOCK", () => {
    it("accepts a valid BLOCK with expiresAt", () => {
        const result = adminActionSchema.safeParse(validBlock);
        expect(result.success).toBe(true);
    });

    it("rejects BLOCK when expiresAt is missing (block must be temporary)", () => {
        const result = adminActionSchema.safeParse({
            ...validBlock,
            expiresAt: undefined,
        });
        expect(result.success).toBe(false);
        if (!result.success) {
            const paths = result.error.issues.map((i) => i.path[0]);
            expect(paths).toContain("expiresAt");
        }
    });
});

// reason field
describe("adminActionSchema - reason", () => {
    it("rejects reason shorter than 5 characters", () => {
        const result = adminActionSchema.safeParse({ ...validBan, reason: "bad" });
        expect(result.success).toBe(false);
    });

    it("rejects reason longer than 256 characters", () => {
        const result = adminActionSchema.safeParse({
            ...validBan,
            reason: "a".repeat(257),
        });
        expect(result.success).toBe(false);
    });

    it("accepts reason at exactly 5 characters", () => {
        const result = adminActionSchema.safeParse({ ...validBan, reason: "abcde" });
        expect(result.success).toBe(true);
    });

    it("accepts reason at exactly 256 characters", () => {
        const result = adminActionSchema.safeParse({
            ...validBan,
            reason: "a".repeat(256),
        });
        expect(result.success).toBe(true);
    });
});

// targetType field
describe("adminActionSchema - targetType", () => {
    it("accepts USER, PRODUCT, REVIEW as valid target types", () => {
        for (const targetType of ["USER", "PRODUCT", "REVIEW"] as const) {
            const result = adminActionSchema.safeParse({ ...validBan, targetType });
            expect(result.success).toBe(true);
        }
    });

    it("rejects unknown target types", () => {
        const result = adminActionSchema.safeParse({
            ...validBan,
            targetType: "COMMENT",
        });
        expect(result.success).toBe(false);
    });
});

// targetId field
describe("adminActionSchema - targetId", () => {
    it("rejects empty targetId", () => {
        const result = adminActionSchema.safeParse({ ...validBan, targetId: "" });
        expect(result.success).toBe(false);
    });
});
