import React, { useState } from "react";
import { getByCode, PromoCodeDto } from "../../services/promoCodeService";
import "./PromoCodeModal.scss";

interface Props {
    subtotal: number;
    confirmLabel?: string;
    onConfirm: (promoCode?: string) => void;
    onClose: () => void;
}

function calcDiscount(promo: PromoCodeDto, subtotal: number): number {
    if (promo.promoCodeType === "PERCENTAGE") {
        return subtotal * (promo.discountValue / 100);
    }
    // FIXED_AMOUNT
    return Math.min(promo.discountValue, subtotal);
}

function discountLabel(promo: PromoCodeDto): string {
    if (promo.promoCodeType === "PERCENTAGE") {
        return promo.discountValue + "% off";
    }
    return "$" + promo.discountValue.toFixed(2) + " off";
}

const PromoCodeModal: React.FC<Props> = ({
    subtotal,
    confirmLabel = "Proceed to checkout",
    onConfirm,
    onClose,
}) => {
    const [codeInput, setCodeInput] = useState("");
    const [applied, setApplied] = useState<PromoCodeDto | null>(null);
    const [applying, setApplying] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const discount = applied ? calcDiscount(applied, subtotal) : 0;
    const total = subtotal - discount;

    const handleApply = async () => {
        const trimmed = codeInput.trim().toUpperCase();
        if (!trimmed) return;
        setError(null);
        setApplying(true);
        try {
            const result = await getByCode(trimmed);
            if (!result.active) {
                setError("This promo code is no longer active.");
            } else {
                setApplied(result);
                setCodeInput("");
            }
        } catch (err: any) {
            const msg = err?.response?.data?.message;
            if (err?.response?.status === 404) {
                setError("Promo code not found. Check the spelling and try again.");
            } else if (msg) {
                setError(msg);
            } else {
                setError("Could not apply the code. Please try again.");
            }
        } finally {
            setApplying(false);
        }
    };

    const handleRemove = () => {
        setApplied(null);
        setError(null);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") handleApply();
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={e => e.stopPropagation()}>
                <button className="modal__close" onClick={onClose} aria-label="Close">&#x2715;</button>

                <div className="promo-modal">
                    <div>
                        <h2 className="promo-modal__title">Do you have a promo code?</h2>
                        <p className="promo-modal__subtitle">
                            Enter it below for a discount, or skip and go straight to checkout.
                        </p>
                    </div>

                    {!applied ? (
                        <>
                            <div className="promo-input-row">
                                <input
                                    type="text"
                                    placeholder="e.g. SUMMER20"
                                    value={codeInput}
                                    onChange={e => { setCodeInput(e.target.value.toUpperCase()); setError(null); }}
                                    onKeyDown={handleKeyDown}
                                    className={error ? "is-invalid" : ""}
                                    autoFocus
                                />
                                <button
                                    type="button"
                                    className={"btn btn--secondary" + (applying ? " btn--loading" : "")}
                                    onClick={handleApply}
                                    disabled={applying || !codeInput.trim()}
                                >
                                    Apply
                                </button>
                            </div>
                            {error && <p className="promo-error">{error}</p>}
                        </>
                    ) : (
                        <div className="promo-discount-badge">
                            <span className="promo-discount-badge__icon">🎉</span>
                            <div className="promo-discount-badge__text">
                                <strong>{discountLabel(applied)} with code &ldquo;{applied.code}&rdquo;</strong>
                                <span>You save ${discount.toFixed(2)}</span>
                            </div>
                            <button className="promo-discount-badge__remove" onClick={handleRemove}>
                                Remove
                            </button>
                        </div>
                    )}

                    <div className="promo-summary">
                        <div className="promo-summary__row">
                            <span>Subtotal</span>
                            <span>${subtotal.toFixed(2)}</span>
                        </div>
                        {applied && (
                            <div className="promo-summary__row">
                                <span>Discount ({discountLabel(applied)})</span>
                                <span className="promo-summary__savings">- ${discount.toFixed(2)}</span>
                            </div>
                        )}
                        <hr className="promo-summary__divider" />
                        <div className="promo-summary__row">
                            <span className="promo-summary__total">Total</span>
                            <span className="promo-summary__total">${total.toFixed(2)}</span>
                        </div>
                    </div>

                    <div className="promo-actions">
                        <button
                            className="btn btn--accent btn--lg"
                            onClick={() => onConfirm(applied?.code)}
                        >
                            {confirmLabel}
                        </button>
                        <button className="btn btn--ghost btn--lg" onClick={() => onConfirm(undefined)}>
                            Skip, no code
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PromoCodeModal;
