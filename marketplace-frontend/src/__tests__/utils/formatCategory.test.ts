import { formatCategory } from "../../utils/formatCategory";

describe("formatCategory", () => {
    it("converts a single-word category to title case", () => {
        expect(formatCategory("ELECTRONICS")).toBe("Electronics");
    });

    it("converts an underscore-separated category to separate words in title case", () => {
        expect(formatCategory("MOBILE_PHONES")).toBe("Mobile Phones");
    });

    it("handles three-word categories", () => {
        expect(formatCategory("HOME_AND_GARDEN")).toBe("Home And Garden");
    });

    it("handles already-lowercase input", () => {
        expect(formatCategory("clothing")).toBe("Clothing");
    });

    it("handles mixed-case input", () => {
        expect(formatCategory("sPoRtS")).toBe("Sports");
    });

    it("returns an empty string when given an empty string", () => {
        expect(formatCategory("")).toBe("");
    });

    it("preserves single-character words", () => {
        expect(formatCategory("A_B_C")).toBe("A B C");
    });
});
