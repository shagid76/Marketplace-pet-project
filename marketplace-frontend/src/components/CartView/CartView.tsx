import React, { useState } from "react";
import CartItemCard from "../CartItemCard/CartItemCard";
import { Product } from "../../types/Product/Product";
import { checkByCode, PromoCodeDto } from "../../services/promoCodeService";
import "./CartView.scss";

interface CartViewProps {
    products: Product[];
    onRemove: (id: string) => void;
    buyAll: (promoCode?: string) => void;
}

function calcDiscount(promo: PromoCodeDto, subtotal: number): number {
    if (promo.promoCodeType === "PERCENTAGE") {
        return Math.min(subtotal, subtotal * (promo.discountValue / 100));
    }
    return Math.min(promo.discountValue, subtotal);
}

const CartView: React.FC<CartViewProps> = ({ products, onRemove, buyAll }) => {
    const [promoInput, setPromoInput] = useState("");
    const [appliedPromo, setAppliedPromo] = useState<PromoCodeDto | null>(null);
    const [applyError, setApplyError] = useState<string | null>(null);
    const [isApplying, setIsApplying] = useState(false);

    const bannedProducts = products.filter((p) => p.productStatus === "BANNED");
    const hasBanned = bannedProducts.length > 0;

    if (products.length === 0) {
        return (
            <div className="cart">
                <div className="cart__empty">
                    <h3>Your cart is empty</h3>
                    <p>Browse the marketplace and add something you like.</p>
                </div>
            </div>
        );
    }

    const availableProducts = products.filter((p) => p.productStatus !== "BANNED");
    const subtotal = availableProducts.reduce((sum, p) => sum + p.price, 0);
    const discount = appliedPromo ? calcDiscount(appliedPromo, subtotal) : 0;
    const total = subtotal - discount;

    const handleApply = async () => {
        const code = promoInput.trim().toUpperCase();
        if (!code) return;
        setIsApplying(true);
        setApplyError(null);
        try {
            const promo = await checkByCode(code, availableProducts.map((p) => p.id));
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

    const handleCheckout = () => {
        if (hasBanned) return;
        const code = promoInput.trim() || undefined;
        buyAll(code);
    };

    return (
        <div className="cart">
            <div>
                <div className="cart__header">
                    <h2>Your cart</h2>
                    <p className="u-text-muted">
                        {products.length} item(s)
                        {hasBanned && (
                            <> — <span style={{ color: "var(--color-danger-600, #dc2626)" }}>
                                {bannedProducts.length} banned
                            </span></>
                        )}
                    </p>
                </div>

                <ul className="cart__list">
                    {products.map((product) => (
                        <CartItemCard
                            key={product.id}
                            id={product.id}
                            title={product.title}
                            price={product.price}
                            isBanned={product.productStatus === "BANNED"}
                            onRemove={onRemove}
                        />
                    ))}
                </ul>
            </div>

            <aside className="cart__summary">
                <h3>Order summary</h3>

                <div className="cart__summary-row">
                    <span>Subtotal{hasBanned ? " (available)" : ""}</span>
                    <strong>${subtotal.toFixed(2)}</strong>
                </div>

                <div className="cart__promo">
                    <p className="cart__promo-label">Promo code</p>

                    {appliedPromo ? (
                        <div className="cart__promo-applied">
                            <span className="cart__promo-badge">
                                {appliedPromo.code}
                                {appliedPromo.promoCodeType === "PERCENTAGE"
                                    ? ` — ${appliedPromo.discountValue}% off`
                                    : ` — $${appliedPromo.discountValue.toFixed(2)} off`}
                            </span>
                            <button
                                type="button"
                                className="cart__promo-remove"
                                onClick={handleRemovePromo}
                                aria-label="Remove promo code"
                            >
                                ✕
                            </button>
                        </div>
                    ) : (
                        <div className="cart__promo-row">
                            <input
                                className="cart__promo-input"
                                type="text"
                                placeholder="ENTER CODE"
                                value={promoInput}
                                onChange={handleInputChange}
                                onKeyDown={(e) => e.key === "Enter" && handleApply()}
                                maxLength={32}
                                spellCheck={false}
                                disabled={hasBanned}
                            />
                            <button
                                type="button"
                                className={"btn btn--secondary btn--sm" + (isApplying ? " btn--loading" : "")}
                                onClick={handleApply}
                                disabled={isApplying || !promoInput.trim() || hasBanned}
                            >
                                Apply
                            </button>
                        </div>
                    )}

                    {applyError && <p className="cart__promo-error">{applyError}</p>}
                </div>

                {appliedPromo && discount > 0 && (
                    <>
                        <div className="cart__summary-row cart__summary-row--discount">
                            <span>Discount</span>
                            <strong className="u-text-success">-${discount.toFixed(2)}</strong>
                        </div>
                        <div className="cart__summary-divider" />
                        <div className="cart__summary-row">
                            <span>Total</span>
                            <strong>${total.toFixed(2)}</strong>
                        </div>
                    </>
                )}

                {hasBanned && (
                    <p className="cart__banned-warning">
                        Remove banned items before checking out.
                    </p>
                )}

                <button
                    className="btn btn--accent btn--block btn--lg"
                    onClick={handleCheckout}
                    disabled={hasBanned || availableProducts.length === 0}
                    title={hasBanned ? "Remove banned items first" : undefined}
                >
                    Checkout
                </button>

                {!promoInput.trim() && !hasBanned && (
                    <p className="cart__promo-hint">
                        Have a promo code? Enter it above or we'll ask you at checkout.
                    </p>
                )}
            </aside>
        </div>
    );
};

export default CartView;
