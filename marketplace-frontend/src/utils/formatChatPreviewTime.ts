/**
 * Formats an ISO timestamp for the chat list preview.
 * - Same day (today) -> "HH:MM"
 * - Any other day    -> "DD/MM/YY"
 */
export function formatChatPreviewTime(iso?: string | null): string {
    if (!iso) return "";

    const date = new Date(iso);
    const now = new Date();

    if (date.toDateString() !== now.toDateString()) {
        return date.toLocaleDateString([], {
            day: "2-digit",
            month: "2-digit",
            year: "2-digit",
        });
    }

    return date.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
    });
}
