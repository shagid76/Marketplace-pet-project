import { productCreateSchema } from "../../validation/productCreateSchema";

const makeFile = (name = "photo.jpg") => new File(["content"], name, { type: "image/jpeg" });

const validInput = {
    title: "Nice vintage camera",
    description: "A beautiful film camera from the 1970s, fully working.",
    price: 120,
    category: "ELECTRONICS",
    images: [makeFile("a.jpg"), makeFile("b.jpg")],
};

// title
describe("productCreateSchema - title", () => {
    it("accepts a valid title", () => {
        expect(productCreateSchema.safeParse(validInput).success).toBe(true);
    });

    it("rejects title shorter than 5 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, title: "cam" }).success).toBe(false);
    });

    it("rejects title longer than 128 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, title: "a".repeat(129) }).success).toBe(false);
    });

    it("accepts title at exactly 5 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, title: "Canon" }).success).toBe(true);
    });

    it("accepts title at exactly 128 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, title: "a".repeat(128) }).success).toBe(true);
    });
});

// description
describe("productCreateSchema - description", () => {
    it("rejects description shorter than 10 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, description: "Too short" }).success).toBe(false);
    });

    it("rejects description longer than 256 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, description: "a".repeat(257) }).success).toBe(false);
    });

    it("accepts description at exactly 10 characters", () => {
        expect(productCreateSchema.safeParse({ ...validInput, description: "1234567890" }).success).toBe(true);
    });
});

// price
describe("productCreateSchema - price", () => {
    it("rejects zero price", () => {
        expect(productCreateSchema.safeParse({ ...validInput, price: 0 }).success).toBe(false);
    });

    it("rejects negative price", () => {
        expect(productCreateSchema.safeParse({ ...validInput, price: -5 }).success).toBe(false);
    });

    it("accepts price of 0.01", () => {
        expect(productCreateSchema.safeParse({ ...validInput, price: 0.01 }).success).toBe(true);
    });

    it("coerces a string number to a number type", () => {
        const result = productCreateSchema.safeParse({ ...validInput, price: "99" });
        expect(result.success).toBe(true);
        if (result.success) {
            expect(typeof result.data.price).toBe("number");
            expect(result.data.price).toBe(99);
        }
    });
});

// images
describe("productCreateSchema - images", () => {
    it("rejects fewer than 2 images", () => {
        expect(productCreateSchema.safeParse({ ...validInput, images: [makeFile()] }).success).toBe(false);
    });

    it("rejects more than 6 images", () => {
        const images = Array.from({ length: 7 }, (_, i) => makeFile(`img${i}.jpg`));
        expect(productCreateSchema.safeParse({ ...validInput, images }).success).toBe(false);
    });

    it("accepts exactly 2 images", () => {
        expect(productCreateSchema.safeParse({ ...validInput, images: [makeFile("a.jpg"), makeFile("b.jpg")] }).success).toBe(true);
    });

    it("accepts exactly 6 images", () => {
        const images = Array.from({ length: 6 }, (_, i) => makeFile(`img${i}.jpg`));
        expect(productCreateSchema.safeParse({ ...validInput, images }).success).toBe(true);
    });
});

// category
describe("productCreateSchema - category", () => {
    it("rejects empty category", () => {
        expect(productCreateSchema.safeParse({ ...validInput, category: "" }).success).toBe(false);
    });

    it("accepts a valid category string", () => {
        expect(productCreateSchema.safeParse({ ...validInput, category: "CLOTHING" }).success).toBe(true);
    });
});
