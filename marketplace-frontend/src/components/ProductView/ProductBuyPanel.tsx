import { useState } from "react";
import { checkByCode, PromoCodeDto } from "../../services/promoCodeService";

interface Props {
    productId: string;
    price: number;
    createBuyUrl: (ids: string[], promoCode?: string) => void;
}

function calcDiscount(promo: PromoCodeDto, price: number): number {
    if (promo.promoCodeType === "PERCENTAGE") {
        return Math.min(price, price * (promo.discountValue / 100));
    }
    return Math.min(promo.discountValue, price);
}

const ProductBuyPanel: React.FC<Props> = ({ productId, price, createBuyUrl }) => {
    const [promoInput, setPromoInput] = useState("");
    const [appliedPromo, setAppliedPromo] = useState<PromoCodeDto | null>(null);
    const [applyError, setApplyError] = useState<string | null>(null);
    const [isApplying, setIsApplying] = useState(false);

    const discount = appliedPromo ? calcDiscount(appliedPromo, price) : 0;
    const finalPrice = price - discount;

    const handleApply = async () => {
        const code = promoInput.trim().toUpperCase();
        if (!code) return;
        setIsApplying(true);
        setApplyError(null);
        try {
            const promo = await checkByCode(code, [productId]);
            if (!promo.active) {
                setApplyError("This promo code is no longer active.");
                setAppliedPromo(null);
            } else {
                setAppliedPromo(promo);
                setPromoInput(promo.code);
            }
        } catch (err: any) {
            setApplyError(err?.response?.data?.message || "Invalid or expired promo code.");
            setAppliedPromo(null);
        } finally {
            setIsApplying(false);
        }
    };

    const handleRemovePromo = () => {
        setAppliedPromo(null);
        setPromoInput("");
        setApplyError(null);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setPromoInput(e.target.value.toUpperCase());
        if (appliedPromo) setAppliedPromo(null);
        setApplyError(null);
    };

    return (
        <div className="product-detail__buy">
            <div className="product-promo">
                <p className="product-promo__label">Promo code (optional)</p>

                {appliedPromo ? (
                    <div className="product-promo__applied">
                        <span className="product-promo__badge">
                            {appliedPromo.code}
                            {appliedPromo.promoCodeType === "PERCENTAGE"
                                ? ` — ${appliedPromo.discountValue}% off`
                                : ` — $${appliedPromo.discountValue.toFixed(2)} off`}
                        </span>
                        <button
                            type="button"
                            className="product-promo__remove"
                            onClick={handleRemovePromo}
                            aria-label="Remove promo code"
                        >
                            &#x2715;
                        </button>
                    </div>
                ) : (
                    <div className="product-promo__row">
                        <input
                            className="product-promo__input"
                            type="text"
                            placeholder="ENTER CODE"
                            value={promoInput}
                            onChange={handleInputChange}
                            onKeyDown={(e) => e.key === "Enter" && handleApply()}
                            maxLength={32}
                            spellCheck={false}
                        />
                        <button
                            type="button"
                            className={"btn btn--secondary btn--sm" + (isApplying ? " btn--loading" : "")}
                            onClick={handleApply}
                            disabled={isApplying || !promoInput.trim()}
                        >
                            Apply
                        </button>
                    </div>
                )}

                {applyError && <p className="product-promo__error">{applyError}</p>}
            </div>

            <button
                className="btn btn--accent btn--block btn--lg"
                onClick={() => createBuyUrl([productId], promoInput.trim() || undefined)}
            >
                {discount > 0 ? `Buy now — $${finalPrice.toFixed(2)}` : "Buy now"}
            </button>

            {!promoInput.trim() && (
                <p className="product-promo__hint">Have a promo code? Enter it above before buying.</p>
            )}
        </div>
    );
};

export default ProductBuyPanel;
