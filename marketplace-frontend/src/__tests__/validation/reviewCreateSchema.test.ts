import { reviewCreateSchema } from "../../validation/reviewCreateSchema";

describe("reviewCreateSchema", () => {
    const valid = {
        description: "Great seller, fast shipping!",
        rating: 4.5,
        targetId: "user-99",
    };

    it("passes with valid data", () => {
        expect(reviewCreateSchema.safeParse(valid).success).toBe(true);
    });

    // description
    it("fails when description is too short (under 10 chars)", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "Too short" });
        expect(result.success).toBe(false);
    });

    it("passes when description is exactly 10 characters", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "1234567890" });
        expect(result.success).toBe(true);
    });

    it("fails when description exceeds 256 characters", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "a".repeat(257) });
        expect(result.success).toBe(false);
    });

    it("passes when description is exactly 256 characters", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "a".repeat(256) });
        expect(result.success).toBe(true);
    });

    it("trims whitespace from description", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "  Great product!  " });
        expect(result.success).toBe(true);
    });

    it("fails when description is empty after trimming", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, description: "   " });
        expect(result.success).toBe(false);
    });

    // rating
    it("fails when rating is below 0", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, rating: -1 });
        expect(result.success).toBe(false);
    });

    it("passes when rating is 0", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, rating: 0 });
        expect(result.success).toBe(true);
    });

    it("passes when rating is 5", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, rating: 5 });
        expect(result.success).toBe(true);
    });

    it("fails when rating exceeds 5", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, rating: 5.1 });
        expect(result.success).toBe(false);
    });

    it("coerces a string rating to a number", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, rating: "4" });
        expect(result.success).toBe(true);
        if (result.success) expect(result.data.rating).toBe(4);
    });

    // targetId
    it("fails when targetId is empty", () => {
        const result = reviewCreateSchema.safeParse({ ...valid, targetId: "" });
        expect(result.success).toBe(false);
    });
});
