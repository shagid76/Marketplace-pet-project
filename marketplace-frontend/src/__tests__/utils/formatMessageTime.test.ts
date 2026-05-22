import { formatMessageTime } from "../../utils/formatMessageTime";

const msAgo = (ms: number) => new Date(Date.now() - ms).toISOString();

const ONE_HOUR = 60 * 60 * 1000;
const TWENTY_FIVE_HOURS = 25 * 60 * 60 * 1000;

// Recent messages: under 24 hours old
describe("formatMessageTime - recent messages", () => {
    it("does not include a date (no slash) for a message 1 hour ago", () => {
        expect(formatMessageTime(msAgo(ONE_HOUR))).not.toContain("/");
    });

    it("does not include a date for a message 1 minute ago", () => {
        expect(formatMessageTime(msAgo(60_000))).not.toContain("/");
    });

    it("includes a colon (time part is present) for a recent message", () => {
        expect(formatMessageTime(msAgo(ONE_HOUR))).toContain(":");
    });
});

// Old messages: more than 24 hours old
describe("formatMessageTime - old messages", () => {
    it("includes a date component (slash) for a message 25 hours ago", () => {
        expect(formatMessageTime(msAgo(TWENTY_FIVE_HOURS))).toContain("/");
    });

    it("returns date and time parts separated by a space", () => {
        const result = formatMessageTime(msAgo(TWENTY_FIVE_HOURS));
        const parts = result.split(" ");
        // At minimum ["DD/MM/YY", "HH:MM"] - time may include AM/PM so >= 2 parts
        expect(parts.length).toBeGreaterThanOrEqual(2);
        // First part is the date in DD/MM/YY format
        expect(parts[0]).toMatch(/\d{2}\/\d{2}\/\d{2}/);
        // Time colon is present somewhere in the result
        expect(result).toContain(":");
    });

    it("handles a very old message (1 year ago)", () => {
        const result = formatMessageTime(msAgo(365 * 24 * ONE_HOUR));
        expect(result).toContain("/");
        expect(result).toContain(":");
    });
});

// Boundary: exactly 24 hours uses strict > so it returns time only
describe("formatMessageTime - boundary", () => {
    it("does not include a date at exactly 24 hours (strict > comparison)", () => {
        const result = formatMessageTime(msAgo(24 * 60 * 60 * 1000));
        expect(result).not.toContain("/");
    });
});
