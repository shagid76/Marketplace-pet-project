import { User } from "../types/User/User";
import api from "../api/axiosInstance";
import { jwtDecode } from "jwt-decode";
import { LoginFormValues } from "../validation/loginSchema";
import { RegistrationFormValues } from "../validation/registrationSchema";

interface JwtPayload {
    sub: string;
    userId?: string;
    exp?: number;
}

interface AuthResponse {
    user: User;
    token: string;
    refreshToken: string;
}


export const login = async (data: LoginFormValues): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>("/auth/sign-in", data);
    localStorage.setItem("accessToken", response.data.token);
    localStorage.setItem("refreshToken", response.data.refreshToken);
    window.dispatchEvent(new StorageEvent("storage", { key: "accessToken", newValue: response.data.token }));
    return response.data;
};

export const registration = async (data: RegistrationFormValues): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>(`/auth/registration`, data)
    return response.data;
}

export function getCurrentUserId(): string | null {
    const token = localStorage.getItem("accessToken");
    if (!token) return null;
    try {
        const decoded = jwtDecode<JwtPayload>(token);
        return decoded.userId ?? decoded.sub;
    } catch {
        return null;
    }
}