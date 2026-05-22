import { renderHook, act } from "@testing-library/react";
import { useAuth } from "../../hooks/useAuth";
import { jwtDecode } from "jwt-decode";

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

jest.mock("jwt-decode");
const mockJwtDecode = jwtDecode as jest.MockedFunction<typeof jwtDecode>;

// Prevent full page navigation in tests
const originalHref = Object.getOwnPropertyDescriptor(window, "location")!;
beforeAll(() => {
    Object.defineProperty(window, "location", {
        configurable: true,
        value: { href: "" },
    });
});
afterAll(() => {
    Object.defineProperty(window, "location", originalHref);
});

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const FUTURE_EXP = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
const PAST_EXP = Math.floor(Date.now() / 1000) - 3600;   // 1 hour ago

function setToken(token: string | null) {
    if (token === null) {
        localStorage.removeItem("accessToken");
    } else {
        localStorage.setItem("accessToken", token);
    }
}

function fireStorageEvent() {
    window.dispatchEvent(new StorageEvent("storage", { key: "accessToken" }));
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe("useAuth", () => {
    beforeEach(() => {
        localStorage.clear();
        jest.clearAllMocks();
    });

    // -----------------------------------------------------------------------
    // Initial state
    // -----------------------------------------------------------------------

    describe("initial auth state", () => {
        it("returns isAuthenticated=false when no token is stored", () => {
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: ["ROLE_USER"] } as any);
            const { result } = renderHook(() => useAuth());

            expect(result.current.isAuthenticated).toBe(false);
        });

        it("returns isAuthenticated=true for a valid, non-expired token", () => {
            setToken("valid.jwt.token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: ["ROLE_USER"] } as any);

            const { result } = renderHook(() => useAuth());

            expect(result.current.isAuthenticated).toBe(true);
        });

        it("returns isAuthenticated=false for an expired token", () => {
            setToken("expired.jwt.token");
            mockJwtDecode.mockReturnValue({ exp: PAST_EXP, roles: ["ROLE_USER"] } as any);

            const { result } = renderHook(() => useAuth());

            expect(result.current.isAuthenticated).toBe(false);
        });

        it("returns isAuthenticated=false when jwtDecode throws", () => {
            setToken("malformed-token");
            mockJwtDecode.mockImplementation(() => { throw new Error("bad token"); });

            const { result } = renderHook(() => useAuth());

            expect(result.current.isAuthenticated).toBe(false);
        });
    });

    // -----------------------------------------------------------------------
    // Roles
    // -----------------------------------------------------------------------

    describe("roles", () => {
        it("exposes roles from the JWT payload when token is valid", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({
                exp: FUTURE_EXP,
                roles: ["ROLE_USER", "ROLE_ADMIN"],
            } as any);

            const { result } = renderHook(() => useAuth());

            expect(result.current.userRoles).toEqual(["ROLE_USER", "ROLE_ADMIN"]);
        });

        it("returns empty roles when token is expired", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: PAST_EXP, roles: ["ROLE_USER"] } as any);

            const { result } = renderHook(() => useAuth());

            expect(result.current.userRoles).toEqual([]);
        });

        it("returns empty roles when payload has no roles field", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP } as any);

            const { result } = renderHook(() => useAuth());

            expect(result.current.userRoles).toEqual([]);
        });
    });

    // -----------------------------------------------------------------------
    // hasRole
    // -----------------------------------------------------------------------

    describe("hasRole()", () => {
        beforeEach(() => {
            setToken("token");
            mockJwtDecode.mockReturnValue({
                exp: FUTURE_EXP,
                roles: ["ROLE_USER"],
            } as any);
        });

        it("returns true when the user has one of the allowed roles", () => {
            const { result } = renderHook(() => useAuth());
            expect(result.current.hasRole(["ROLE_USER", "ROLE_ADMIN"])).toBe(true);
        });

        it("returns false when the user does not have any of the allowed roles", () => {
            const { result } = renderHook(() => useAuth());
            expect(result.current.hasRole(["ROLE_ADMIN", "ROLE_MODERATOR"])).toBe(false);
        });

        it("returns false for an empty allowed list", () => {
            const { result } = renderHook(() => useAuth());
            expect(result.current.hasRole([])).toBe(false);
        });
    });

    // -----------------------------------------------------------------------
    // Reactivity to storage events
    // -----------------------------------------------------------------------

    describe("storage event reactivity", () => {
        it("updates to authenticated when a valid token is stored and storage event fires", () => {
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: ["ROLE_USER"] } as any);
            const { result } = renderHook(() => useAuth());

            // Initially unauthenticated
            expect(result.current.isAuthenticated).toBe(false);

            act(() => {
                setToken("new.valid.token");
                fireStorageEvent();
            });

            expect(result.current.isAuthenticated).toBe(true);
        });

        it("updates to unauthenticated when the token is removed and storage event fires", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: ["ROLE_USER"] } as any);
            const { result } = renderHook(() => useAuth());

            expect(result.current.isAuthenticated).toBe(true);

            act(() => {
                localStorage.removeItem("accessToken");
                fireStorageEvent();
            });

            expect(result.current.isAuthenticated).toBe(false);
        });

        it("updates roles when a new token with different roles is stored", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: ["ROLE_USER"] } as any);
            const { result } = renderHook(() => useAuth());

            expect(result.current.userRoles).toEqual(["ROLE_USER"]);

            act(() => {
                mockJwtDecode.mockReturnValue({
                    exp: FUTURE_EXP,
                    roles: ["ROLE_USER", "ROLE_ADMIN"],
                } as any);
                fireStorageEvent();
            });

            expect(result.current.userRoles).toEqual(["ROLE_USER", "ROLE_ADMIN"]);
        });
    });

    // -----------------------------------------------------------------------
    // handleLogout
    // -----------------------------------------------------------------------

    describe("handleLogout()", () => {
        it("removes tokens from localStorage", () => {
            setToken("token");
            localStorage.setItem("refreshToken", "refresh");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: [] } as any);

            const { result } = renderHook(() => useAuth());

            act(() => {
                result.current.handleLogout();
            });

            expect(localStorage.getItem("accessToken")).toBeNull();
            expect(localStorage.getItem("refreshToken")).toBeNull();
        });

        it("sets isAuthenticated to false", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: [] } as any);
            const { result } = renderHook(() => useAuth());

            act(() => {
                result.current.handleLogout();
            });

            expect(result.current.isAuthenticated).toBe(false);
        });

        it("redirects to /login", () => {
            setToken("token");
            mockJwtDecode.mockReturnValue({ exp: FUTURE_EXP, roles: [] } as any);
            const { result } = renderHook(() => useAuth());

            act(() => {
                result.current.handleLogout();
            });

            expect(window.location.href).toBe("/login");
        });
    });
});
