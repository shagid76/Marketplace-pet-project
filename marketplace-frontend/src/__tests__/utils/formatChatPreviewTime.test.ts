import { formatChatPreviewTime } from "../../utils/formatChatPreviewTime";

describe("formatChatPreviewTime", () => {
    it("returns empty string for null", () => {
        expect(formatChatPreviewTime(null)).toBe("");
    });

    it("returns empty string for undefined", () => {
        expect(formatChatPreviewTime(undefined)).toBe("");
    });

    it("returns time (no date) for a timestamp from earlier today", () => {
        // Pin "now" to 13:00 so "1 hour ago" (12:00) is always the same calendar day
        const fakeNow = new Date();
        fakeNow.setHours(13, 0, 0, 0);
        jest.useFakeTimers();
        jest.setSystemTime(fakeNow);

        const oneHourAgo = new Date(fakeNow.getTime() - 60 * 60 * 1000);
        const result = formatChatPreviewTime(oneHourAgo.toISOString());

        jest.useRealTimers();

        expect(result).not.toContain("/");
        expect(result.length).toBeGreaterThan(0);
    });

    it("returns date (with slashes) for a timestamp from yesterday", () => {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        const result = formatChatPreviewTime(yesterday.toISOString());
        expect(result).toContain("/");
    });

    it("returns date for a timestamp from 2 days ago", () => {
        const twoDaysAgo = new Date();
        twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
        const result = formatChatPreviewTime(twoDaysAgo.toISOString());
        expect(result).toContain("/");
    });

    it("returns date for a timestamp from last year", () => {
        const lastYear = new Date();
        lastYear.setFullYear(lastYear.getFullYear() - 1);
        const result = formatChatPreviewTime(lastYear.toISOString());
        expect(result).toContain("/");
    });

    it("returns non-empty string for a recent timestamp from today", () => {
        const fiveMinutesAgo = new Date(Date.now() - 5 * 60 * 1000);
        const result = formatChatPreviewTime(fiveMinutesAgo.toISOString());
        expect(result).not.toBe("");
    });
});
