import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import BannedAccountModal from "../../components/BannedAccountModal/BannedAccountModal";

describe("BannedAccountModal", () => {
    const onAcknowledge = jest.fn();

    beforeEach(() => {
        onAcknowledge.mockClear();
    });

    it("shows banned title when type is banned", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        expect(screen.getByText("Your account has been banned")).toBeInTheDocument();
    });

    it("shows blocked title when type is blocked", () => {
        render(<BannedAccountModal type="blocked" onAcknowledge={onAcknowledge} />);
        expect(screen.getByText("Your account has been blocked")).toBeInTheDocument();
    });

    it("shows default banned description when no message provided", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        expect(
            screen.getByText(/permanently banned your account/i)
        ).toBeInTheDocument();
    });

    it("shows default blocked description when type is blocked and no message", () => {
        render(<BannedAccountModal type="blocked" onAcknowledge={onAcknowledge} />);
        expect(
            screen.getByText(/temporarily blocked your account/i)
        ).toBeInTheDocument();
    });

    it("shows custom message when provided", () => {
        render(
            <BannedAccountModal
                type="banned"
                message="You were banned for posting illegal content."
                onAcknowledge={onAcknowledge}
            />
        );
        expect(
            screen.getByText(/posting illegal content/i)
        ).toBeInTheDocument();
    });

    it("always shows the support hint text", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        expect(screen.getByText(/contact support/i)).toBeInTheDocument();
    });

    it("renders a Sign out button", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        expect(screen.getByRole("button", { name: /sign out/i })).toBeInTheDocument();
    });

    it("calls onAcknowledge when Sign out button is clicked", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        fireEvent.click(screen.getByRole("button", { name: /sign out/i }));
        expect(onAcknowledge).toHaveBeenCalledTimes(1);
    });

    it("renders the ban icon", () => {
        render(<BannedAccountModal type="banned" onAcknowledge={onAcknowledge} />);
        expect(screen.getByText("🚫")).toBeInTheDocument();
    });
});
