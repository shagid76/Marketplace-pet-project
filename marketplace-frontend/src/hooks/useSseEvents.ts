import { useEffect, useRef } from "react";
import { API_ROOT } from "../config/api";

const RETRY_DELAY_MS = 3000;

interface SseHandlers {
    onBanned: (message: string) => void;
    onBlocked: (message: string) => void;
}

// Attempt a token refresh; returns new access token or null
async function tryRefreshToken(): Promise<string | null> {
    try {
        const refreshToken = localStorage.getItem("refreshToken");
        if (!refreshToken) return null;
        const res = await fetch(`${API_ROOT}/auth/refresh`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken }),
        });
        if (!res.ok) return null;
        const data = await res.json();
        const newAccess = data.token;
        const newRefresh = data.refreshToken;
        localStorage.setItem("accessToken", newAccess);
        if (newRefresh) localStorage.setItem("refreshToken", newRefresh);
        return newAccess;
    } catch {
        return null;
    }
}

export const useSseEvents = ({ onBanned, onBlocked }: SseHandlers) => {
    const handlersRef = useRef({ onBanned, onBlocked });
    const triggeredRef = useRef(false);

    useEffect(() => {
        handlersRef.current = { onBanned, onBlocked };
    });

    useEffect(() => {
        let active = true;
        const controller = new AbortController();

        const connect = async () => {
            while (active) {
                if (triggeredRef.current) break;

                const token = localStorage.getItem("accessToken");
                if (!token) {
                    await new Promise((res) => setTimeout(res, RETRY_DELAY_MS));
                    continue;
                }

                try {
                    const response = await fetch(`${API_ROOT}/events/subscribe`, {
                        headers: {
                            Authorization: `Bearer ${token}`,
                            Accept: "text/event-stream",
                            "Cache-Control": "no-cache",
                        },
                        signal: controller.signal,
                    });

                    // 401 = token expired — try to refresh silently, then reconnect
                    if (response.status === 401) {
                        const newToken = await tryRefreshToken();
                        if (newToken) {
                            // refresh succeeded — loop will pick up new token
                            continue;
                        }
                        // refresh failed — session is truly over, redirect to login
                        localStorage.removeItem("accessToken");
                        localStorage.removeItem("refreshToken");
                        window.location.href = "/login";
                        break;
                    }

                    // 403 = authenticated but banned/blocked
                    if (response.status === 403) {
                        if (!triggeredRef.current) {
                            triggeredRef.current = true;
                            const body = await response.json().catch(() => ({}));
                            const message: string = body?.message || "";
                            const lower = message.toLowerCase();
                            if (lower.includes("block")) {
                                handlersRef.current.onBlocked(message);
                            } else {
                                handlersRef.current.onBanned(
                                    message || "Your account has been restricted by an administrator."
                                );
                            }
                        }
                        break;
                    }

                    if (!response.ok || !response.body) {
                        await new Promise((res) => setTimeout(res, RETRY_DELAY_MS));
                        continue;
                    }

                    const reader = response.body.getReader();
                    const decoder = new TextDecoder();
                    let buffer = "";

                    while (active) {
                        const { done, value } = await reader.read();
                        if (done) break;

                        buffer += decoder.decode(value, { stream: true });

                        const parts = buffer.split("\n\n");
                        buffer = parts.pop() ?? "";

                        for (const part of parts) {
                            if (!part.trim() || triggeredRef.current) continue;

                            let eventName = "";
                            let data = "";

                            for (const line of part.split("\n")) {
                                if (line.startsWith("event:")) {
                                    eventName = line.slice(6).trim();
                                } else if (line.startsWith("data:")) {
                                    data = line.slice(5).trim();
                                }
                            }

                            const signal = eventName || data;

                            if (signal === "ACCOUNT_BANNED") {
                                triggeredRef.current = true;
                                handlersRef.current.onBanned(
                                    "Your account has been banned by an administrator."
                                );
                            } else if (signal === "ACCOUNT_BLOCKED") {
                                triggeredRef.current = true;
                                handlersRef.current.onBlocked(
                                    "Your account has been temporarily blocked."
                                );
                            }
                        }
                    }
                } catch (err: unknown) {
                    if (!active || (err instanceof Error && err.name === "AbortError")) break;
                    await new Promise((res) => setTimeout(res, RETRY_DELAY_MS));
                }
            }
        };

        connect();

        return () => {
            active = false;
            controller.abort();
        };
    }, []);
};
