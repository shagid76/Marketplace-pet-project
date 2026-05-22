import { registrationSchema } from "../../validation/registrationSchema";

const validInput = {
    email: "yaroslav@example.com",
    username: "yaroslav_dev",
    password: "securePass123",
};

// email
describe("registrationSchema - email", () => {
    it("accepts a valid email", () => {
        expect(registrationSchema.safeParse(validInput).success).toBe(true);
    });

    it("rejects an email without @ symbol", () => {
        expect(registrationSchema.safeParse({ ...validInput, email: "notanemail" }).success).toBe(false);
    });

    it("rejects an email shorter than 5 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, email: "a@b" }).success).toBe(false);
    });

    it("rejects an email longer than 128 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, email: "a".repeat(120) + "@test.com" }).success).toBe(false);
    });

    it("rejects an empty email", () => {
        expect(registrationSchema.safeParse({ ...validInput, email: "" }).success).toBe(false);
    });
});

// username
describe("registrationSchema - username", () => {
    it("rejects username shorter than 5 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, username: "abc" }).success).toBe(false);
    });

    it("rejects username longer than 128 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, username: "a".repeat(129) }).success).toBe(false);
    });

    it("accepts username at exactly 5 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, username: "abcde" }).success).toBe(true);
    });

    it("accepts username at exactly 128 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, username: "a".repeat(128) }).success).toBe(true);
    });
});

// password
describe("registrationSchema - password", () => {
    it("rejects password shorter than 8 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, password: "abc123" }).success).toBe(false);
    });

    it("rejects password longer than 128 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, password: "a".repeat(129) }).success).toBe(false);
    });

    it("accepts password at exactly 8 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, password: "abcd1234" }).success).toBe(true);
    });

    it("accepts password at exactly 128 characters", () => {
        expect(registrationSchema.safeParse({ ...validInput, password: "a".repeat(128) }).success).toBe(true);
    });
});
