/**
 * Formats an ISO timestamp for display in a chat message bubble.
 * - Within the last 24 hours → "HH:MM"
 * - Older than 24 hours      → "DD/MM/YY HH:MM"
 */
export function formatMessageTime(iso: string): string {
    const date = new Date(iso);
    const now = new Date();
    const isOlderThan24h = now.getTime() - date.getTime() > 24 * 60 * 60 * 1000;

    const time = date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

    if (!isOlderThan24h) return time;

    const dateStr = date.toLocaleDateString([], {
        day: "2-digit",
        month: "2-digit",
        year: "2-digit",
    });
    return `${dateStr} ${time}`;
}
