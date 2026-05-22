import { renderHook } from "@testing-library/react";
import { useSseEvents } from "../../hooks/useSseEvents";

// ---------------------------------------------------------------------------
// Helpers: fake fetch / ReadableStream infrastructure
// ---------------------------------------------------------------------------

type FetchCall = {
    url: string;
    init?: RequestInit;
};

/**
 * Build a minimal Response whose body is a ReadableStream of SSE text.
 * Pass an array of raw SSE chunks (e.g. ["event:ACCOUNT_BANNED\n\n"]).
 * The stream will close after all chunks are emitted.
 */
function makeSseResponse(status: number, chunks: string[]): Response {
    if (status !== 200) {
        return new Response(null, { status });
    }

    const encoder = new TextEncoder();
    let index = 0;

    const fakeReader = {
        read: jest.fn().mockImplementation(async () => {
            if (index < chunks.length) {
                return { done: false, value: encoder.encode(chunks[index++]) };
            }
            return { done: true, value: undefined };
        }),
        cancel: jest.fn(),
    };

    const response = new Response(null, { status: 200 });
    Object.defineProperty(response, "body", { value: { getReader: () => fakeReader } });
    return response;
}

// ---------------------------------------------------------------------------
// Test setup
// ---------------------------------------------------------------------------

let fetchCalls: FetchCall[] = [];
let fetchResponses: Response[] = [];

beforeEach(() => {
    fetchCalls = [];
    fetchResponses = [];
    localStorage.clear();

    global.fetch = jest.fn().mockImplementation((url: string, init?: RequestInit) => {
        fetchCalls.push({ url, init });
        const response = fetchResponses.shift();
        if (!response) {
            // Default: never-resolving promise (SSE connection that stays open)
            return new Promise(() => {});
        }
        return Promise.resolve(response);
    });
});

afterEach(() => {
    jest.clearAllMocks();
});

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe("useSseEvents", () => {
    const onBanned = jest.fn();
    const onBlocked = jest.fn();

    beforeEach(() => {
        onBanned.mockClear();
        onBlocked.mockClear();
    });

    // -----------------------------------------------------------------------
    // Connection behaviour
    // -----------------------------------------------------------------------

    describe("connection behaviour", () => {
        it("does not call fetch when no access token is stored", async () => {
            // No token → the loop waits before trying; we unmount immediately
            const { unmount } = renderHook(() =>
                useSseEvents({ onBanned, onBlocked })
            );
            unmount();

            // Allow microtasks to flush
            await Promise.resolve();

            expect(fetch).not.toHaveBeenCalled();
        });

        it("calls the SSE endpoint with the stored access token", async () => {
            localStorage.setItem("accessToken", "my-access-token");

            // Give it a pending response so the hook stays connected
            fetchResponses.push(makeSseResponse(200, []));

            const { unmount } = renderHook(() =>
                useSseEvents({ onBanned, onBlocked })
            );

            await new Promise((r) => setTimeout(r, 50));
            unmount();

            const call = fetchCalls[0];
            expect(call).toBeDefined();
            expect(call.url).toContain("/events/subscribe");
            expect((call.init?.headers as Record<string, string>)["Authorization"]).toBe(
                "Bearer my-access-token"
            );
        });

        it("sets Accept: text/event-stream header", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(makeSseResponse(200, []));

            const { unmount } = renderHook(() =>
                useSseEvents({ onBanned, onBlocked })
            );
            await new Promise((r) => setTimeout(r, 50));
            unmount();

            const headers = fetchCalls[0]?.init?.headers as Record<string, string>;
            expect(headers?.["Accept"]).toBe("text/event-stream");
        });
    });

    // -----------------------------------------------------------------------
    // ACCOUNT_BANNED event
    // -----------------------------------------------------------------------

    describe("ACCOUNT_BANNED event", () => {
        it("calls onBanned when the server sends an ACCOUNT_BANNED SSE event", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(
                makeSseResponse(200, ["event:ACCOUNT_BANNED\ndata:banned\n\n"])
            );

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBanned).toHaveBeenCalledTimes(1);
            expect(onBlocked).not.toHaveBeenCalled();
        });

        it("calls onBanned with a descriptive fallback message", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(
                makeSseResponse(200, ["event:ACCOUNT_BANNED\ndata:\n\n"])
            );

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBanned).toHaveBeenCalledWith(
                expect.stringContaining("banned")
            );
        });

        it("does not call onBanned more than once for repeated events", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(
                makeSseResponse(200, [
                    "event:ACCOUNT_BANNED\ndata:banned\n\n",
                    "event:ACCOUNT_BANNED\ndata:banned\n\n",
                ])
            );

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBanned).toHaveBeenCalledTimes(1);
        });
    });

    // -----------------------------------------------------------------------
    // ACCOUNT_BLOCKED event
    // -----------------------------------------------------------------------

    describe("ACCOUNT_BLOCKED event", () => {
        it("calls onBlocked when the server sends an ACCOUNT_BLOCKED SSE event", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(
                makeSseResponse(200, ["event:ACCOUNT_BLOCKED\ndata:blocked\n\n"])
            );

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBlocked).toHaveBeenCalledTimes(1);
            expect(onBanned).not.toHaveBeenCalled();
        });

        it("calls onBlocked with a descriptive message", async () => {
            localStorage.setItem("accessToken", "tok");
            fetchResponses.push(
                makeSseResponse(200, ["event:ACCOUNT_BLOCKED\ndata:\n\n"])
            );

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBlocked).toHaveBeenCalledWith(
                expect.stringContaining("blocked")
            );
        });
    });

    // -----------------------------------------------------------------------
    // HTTP error responses
    // -----------------------------------------------------------------------

    describe("HTTP 403 response", () => {
        it("calls onBanned when 403 body contains a ban message", async () => {
            localStorage.setItem("accessToken", "tok");

            // Manually craft a 403 Response with a JSON body
            const body = JSON.stringify({ message: "Your account has been banned by an administrator." });
            const response = new Response(body, {
                status: 403,
                headers: { "Content-Type": "application/json" },
            });
            fetchResponses.push(response);

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBanned).toHaveBeenCalledTimes(1);
        });

        it("calls onBlocked when 403 body contains the word 'block'", async () => {
            localStorage.setItem("accessToken", "tok");

            const body = JSON.stringify({ message: "Your account has been blocked." });
            const response = new Response(body, {
                status: 403,
                headers: { "Content-Type": "application/json" },
            });
            fetchResponses.push(response);

            renderHook(() => useSseEvents({ onBanned, onBlocked }));
            await new Promise((r) => setTimeout(r, 100));

            expect(onBlocked).toHaveBeenCalledTimes(1);
        });
    });

    describe("HTTP 401 response", () => {
        it("attempts a token refresh on 401", async () => {
            localStorage.setItem("accessToken", "expired-token");
            localStorage.setItem("refreshToken", "refresh-token");

            // First fetch → 401; refresh fetch → success; second SSE fetch → pending
            fetchResponses.push(new Response(null, { status: 401 }));
            // The refresh POST will also use fetch
            fetchResponses.push(
                new Response(JSON.stringify({ token: "new-token", refreshToken: "new-refresh" }), {
                    status: 200,
                    headers: { "Content-Type": "application/json" },
                })
            );
            fetchResponses.push(makeSseResponse(200, []));

            const { unmount } = renderHook(() =>
                useSseEvents({ onBanned, onBlocked })
            );
            await new Promise((r) => setTimeout(r, 150));
            unmount();

            // Should have made at least 2 fetch calls (SSE + refresh)
            expect(fetchCalls.length).toBeGreaterThanOrEqual(2);
            expect(onBanned).not.toHaveBeenCalled();
            expect(onBlocked).not.toHaveBeenCalled();
        });
    });

    // -----------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------

    describe("cleanup on unmount", () => {
        it("aborts the ongoing fetch when the component unmounts", async () => {
            localStorage.setItem("accessToken", "tok");
            let abortCalled = false;

            global.fetch = jest.fn().mockImplementation((_url: string, init?: RequestInit) => {
                (init?.signal as AbortSignal)?.addEventListener("abort", () => {
                    abortCalled = true;
                });
                return new Promise(() => {}); // never resolves
            });

            const { unmount } = renderHook(() =>
                useSseEvents({ onBanned, onBlocked })
            );
            await new Promise((r) => setTimeout(r, 50));
            unmount();
            await new Promise((r) => setTimeout(r, 20));

            expect(abortCalled).toBe(true);
        });
    });
});
