import { useEffect, useState, useCallback } from "react";
import { jwtDecode } from "jwt-decode";

interface JwtPayload {
    exp?: number;
    roles?: string[];
}

export const useAuth = () => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
    const [userRoles, setUserRoles] = useState<string[]>([]); 

    const checkToken = useCallback(() => {
        const token = localStorage.getItem("accessToken");
        if (!token) return { auth: false, roles: [] }; 

        try {
            const payload = jwtDecode<JwtPayload>(token);
            const isValid = !!payload.exp && payload.exp * 1000 > Date.now();
            return { 
                auth: isValid, 
                roles: isValid ? (payload.roles || []) : [] 
            };
        } catch {
            return { auth: false, roles: [] };
        }
    }, []);

    const hasRole = (allowedRoles: string[]) =>
        userRoles.some((role: string) => allowedRoles.includes(role));

    const handleLogout = useCallback(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        setIsAuthenticated(false);
        setUserRoles([]);
        window.location.href = "/login";
    }, []);

    useEffect(() => {
        const updateAuthState = () => {
            const { auth, roles } = checkToken();
            setIsAuthenticated(auth);
            setUserRoles(roles);
        };

        updateAuthState();
        window.addEventListener("storage", updateAuthState);
        return () => window.removeEventListener("storage", updateAuthState);
    }, [checkToken]);

    return { isAuthenticated, userRoles, hasRole, handleLogout };
};