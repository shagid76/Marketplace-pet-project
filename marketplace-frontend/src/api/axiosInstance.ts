import axios from "axios";
import { API_BASE, API_ROOT } from "../config/api";

const api = axios.create({
    baseURL: API_ROOT,
});

api.interceptors.request.use(config => {
    const token = localStorage.getItem("accessToken");
    const url = config.url || "";
    const isAuthEndpoint =
        url.includes("/auth/sign-in") ||
        url.includes("/auth/registration") ||
        url.includes("/auth/refresh");

    if (token && !isAuthEndpoint) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// --- Response interceptor: refresh token + friendly network errors -------
api.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;

        if (!error.response) {
            console.warn("[api] Network error — is the backend reachable at", API_ROOT, "?");
            error.isNetworkError = true;
            return Promise.reject(error);
        }

        // Skip refresh for auth endpoints so ban/block messages pass through unchanged
        const requestUrl = originalRequest.url || "";
        const isAuthEndpoint =
            requestUrl.includes("/auth/sign-in") ||
            requestUrl.includes("/auth/registration") ||
            requestUrl.includes("/auth/refresh");

        // Only refresh on 401 (token expired).
        // 403 means forbidden/banned — let it pass through to the caller unchanged.
        if (
            error.response.status === 401 &&
            !originalRequest._retry &&
            !isAuthEndpoint
        ) {
            originalRequest._retry = true;
            try {
                const refreshToken = localStorage.getItem("refreshToken");
                if (!refreshToken) throw new Error("No refresh token stored");

                const res = await axios.post(`${API_ROOT}/auth/refresh`, { refreshToken });

                const newAccessToken = res.data.token;
                const newRefreshToken = res.data.refreshToken;

                localStorage.setItem("accessToken", newAccessToken);
                if (newRefreshToken) {
                    localStorage.setItem("refreshToken", newRefreshToken);
                }
                // Notify useAuth in the same tab — storage event only fires cross-tab by default
                window.dispatchEvent(new StorageEvent("storage", { key: "accessToken", newValue: newAccessToken }));

                return api({
                    ...originalRequest,
                    headers: {
                        ...originalRequest.headers,
                        Authorization: `Bearer ${newAccessToken}`,
                    },
                });
            } catch (err) {
                console.error("[api] Refresh token failed — redirecting to login:", err);
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");
                window.location.href = "/login";
                return Promise.reject(err);
            }
        }

        return Promise.reject(error);
    }
);

export { API_BASE, API_ROOT };
export default api;