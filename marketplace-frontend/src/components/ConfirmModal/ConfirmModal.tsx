import React from "react";

interface Props {
    message: string;
    confirmLabel?: string;
    danger?: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

const ConfirmModal: React.FC<Props> = ({
    message,
    confirmLabel = "Confirm",
    danger = false,
    onConfirm,
    onCancel,
}) => (
    <div className="modal-overlay" onClick={onCancel}>
        <div className="modal" onClick={(e) => e.stopPropagation()}>
            <p className="u-text-muted u-mb-4">{message}</p>
            <div className="u-row">
                <button
                    className={`btn ${danger ? "btn--danger" : "btn--primary"}`}
                    onClick={onConfirm}
                >
                    {confirmLabel}
                </button>
                <button className="btn btn--secondary" onClick={onCancel}>
                    Cancel
                </button>
            </div>
        </div>
    </div>
);

export default ConfirmModal;
