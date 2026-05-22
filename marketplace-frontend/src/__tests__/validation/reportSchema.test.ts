import { reportSchema } from "../../validation/reportCreateSchema";

describe("reportSchema", () => {
    const valid = {
        targetType: "PRODUCT",
        targetId: "prod-55",
        description: "This listing is fraudulent.",
    };

    it("passes with valid data", () => {
        expect(reportSchema.safeParse(valid).success).toBe(true);
    });

    // targetType
    it("passes with USER targetType", () => {
        const result = reportSchema.safeParse({ ...valid, targetType: "USER" });
        expect(result.success).toBe(true);
    });

    it("passes with REVIEW targetType", () => {
        const result = reportSchema.safeParse({ ...valid, targetType: "REVIEW" });
        expect(result.success).toBe(true);
    });

    it("fails with an invalid targetType", () => {
        const result = reportSchema.safeParse({ ...valid, targetType: "COMMENT" });
        expect(result.success).toBe(false);
    });

    // targetId
    it("fails when targetId is empty", () => {
        const result = reportSchema.safeParse({ ...valid, targetId: "" });
        expect(result.success).toBe(false);
    });

    // description
    it("fails when description is too short (under 10 chars)", () => {
        const result = reportSchema.safeParse({ ...valid, description: "Short" });
        expect(result.success).toBe(false);
    });

    it("passes when description is exactly 10 characters", () => {
        const result = reportSchema.safeParse({ ...valid, description: "0123456789" });
        expect(result.success).toBe(true);
    });

    it("fails when description exceeds 256 characters", () => {
        const result = reportSchema.safeParse({ ...valid, description: "a".repeat(257) });
        expect(result.success).toBe(false);
    });

    it("passes when description is exactly 256 characters", () => {
        const result = reportSchema.safeParse({ ...valid, description: "a".repeat(256) });
        expect(result.success).toBe(true);
    });

    it("trims whitespace from description", () => {
        const result = reportSchema.safeParse({ ...valid, description: "  Spam report here!  " });
        expect(result.success).toBe(true);
    });

    it("fails when description is empty after trimming", () => {
        const result = reportSchema.safeParse({ ...valid, description: "     " });
        expect(result.success).toBe(false);
    });
});
