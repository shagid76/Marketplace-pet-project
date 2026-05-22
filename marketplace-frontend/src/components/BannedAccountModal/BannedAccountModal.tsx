import React from "react";
import "./BannedAccountModal.scss";

interface Props {
    type: "banned" | "blocked";
    message?: string;
    onAcknowledge: () => void;
}

const BannedAccountModal: React.FC<Props> = ({ type, message, onAcknowledge }) => (
    <div className="banned-account-overlay">
        <div className="banned-account-modal">
            <div className="banned-account-modal__icon">🚫</div>
            <h2 className="banned-account-modal__title">
                {type === "banned"
                    ? "Your account has been banned"
                    : "Your account has been blocked"}
            </h2>
            <p className="banned-account-modal__desc">
                {message
                    ? message
                    : type === "banned"
                        ? "An administrator has permanently banned your account."
                        : "An administrator has temporarily blocked your account."}
                {" "}Contact support if you think this is a mistake.
            </p>
            <button className="btn btn--danger btn--block btn--lg" onClick={onAcknowledge}>
                Sign out
            </button>
        </div>
    </div>
);

export default BannedAccountModal;
