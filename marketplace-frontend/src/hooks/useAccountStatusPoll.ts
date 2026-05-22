import { useEffect, useRef } from "react";
import axios from "axios";

const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";
const API_ROOT = `${API_BASE.replace(/\/$/, "")}/api`;

const POLL_INTERVAL_MS = 10_000;

interface Handlers {
    onBanned: (message: string) => void;
    onBlocked: (message: string) => void;
}

// Attempt a silent token refresh; notifies useAuth in the same tab via StorageEvent
async function tryRefreshToken(): Promise<boolean> {
    try {
        const refreshToken = localStorage.getItem("refreshToken");
        if (!refreshToken) return false;
        const res = await axios.post(`${API_ROOT}/auth/refresh`, { refreshToken });
        const newAccess: string = res.data.token;
        const newRefresh: string = res.data.refreshToken;
        localStorage.setItem("accessToken", newAccess);
        if (newRefresh) localStorage.setItem("refreshToken", newRefresh);
        // Dispatch so useAuth's storage listener fires in the same tab
        window.dispatchEvent(new StorageEvent("storage", { key: "accessToken", newValue: newAccess }));
        return true;
    } catch {
        return false;
    }
}

export const useAccountStatusPoll = ({ onBanned, onBlocked }: Handlers) => {
    const handlersRef = useRef({ onBanned, onBlocked });
    const triggeredRef = useRef(false);

    useEffect(() => {
        handlersRef.current = { onBanned, onBlocked };
    });

    useEffect(() => {
        const check = async () => {
            if (triggeredRef.current) return;

            const token = localStorage.getItem("accessToken");
            if (!token) return;

            try {
                const res = await fetch(`${API_ROOT}/users/me`, {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (res.ok) return;

                // 401 = access token expired — refresh silently and let the next cycle re-poll
                // Do NOT treat this as a ban: the user is authenticated, just needs a new token
                if (res.status === 401) {
                    await tryRefreshToken();
                    return;
                }

                // 403 = authenticated but account is banned or blocked
                if (res.status === 403) {
                    if (triggeredRef.current) return;
                    triggeredRef.current = true;

                    const body = await res.json().catch(() => ({}));
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
            } catch {
            }
        };

        check();
        const id = setInterval(check, POLL_INTERVAL_MS);
        return () => clearInterval(id);
    }, []);
};
