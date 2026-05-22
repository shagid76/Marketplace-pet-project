import { userUpdateSchema } from "../../validation/userUpdateSchema";

describe("userUpdateSchema", () => {
    const valid = {
        username: "cooluser",
    };

    it("passes with just a valid username", () => {
        expect(userUpdateSchema.safeParse(valid).success).toBe(true);
    });

    it("passes with removeAvatar set to true", () => {
        const result = userUpdateSchema.safeParse({ ...valid, removeAvatar: true });
        expect(result.success).toBe(true);
    });

    it("passes with removeAvatar set to false", () => {
        const result = userUpdateSchema.safeParse({ ...valid, removeAvatar: false });
        expect(result.success).toBe(true);
    });

    it("defaults removeAvatar to false when omitted", () => {
        const result = userUpdateSchema.safeParse(valid);
        if (result.success) {
            expect(result.data.removeAvatar).toBe(false);
        }
    });

    it("passes when avatar is undefined (no file selected)", () => {
        const result = userUpdateSchema.safeParse({ ...valid, avatar: undefined });
        expect(result.success).toBe(true);
    });

    // username
    it("fails when username is too short (under 5 chars)", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "ab" });
        expect(result.success).toBe(false);
    });

    it("passes when username is exactly 5 characters", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "abcde" });
        expect(result.success).toBe(true);
    });

    it("fails when username exceeds 128 characters", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "a".repeat(129) });
        expect(result.success).toBe(false);
    });

    it("passes when username is exactly 128 characters", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "a".repeat(128) });
        expect(result.success).toBe(true);
    });

    it("trims whitespace from username", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "  cooluser  " });
        expect(result.success).toBe(true);
    });

    it("fails when username is empty after trimming", () => {
        const result = userUpdateSchema.safeParse({ ...valid, username: "    " });
        expect(result.success).toBe(false);
    });
});
