import React, { useState } from "react";
import CartView from "../../components/CartView/CartView";
import { Product } from "../../types/Product/Product";
import { CreateBuyUrl } from "../../services/paymentService";
import PromoCodeModal from "../../components/PromoCode/PromoCodeModal";
import BannedItemsAlert from "../../components/BannedItemsAlert/BannedItemsAlert";
import { useCartData } from "../../hooks/useCartData";

const CartPage: React.FC = () => {
    const { cart, isLoading, removeFromCart } = useCartData();
    const [showPromoModal, setShowPromoModal] = useState(false);
    const [showBannedAlert, setShowBannedAlert] = useState(true);

    if (isLoading) return <div className="loading-state">Loading cart...</div>;
    if (!cart) return <p>Cart not found.</p>;

    const bannedItems = (cart.products || [])
        .filter((p: Product) => p.productStatus === "BANNED")
        .map((p: Product) => ({ id: p.id, title: p.title }));

    const activeProducts = (cart.products || []).filter((p: Product) => p.productStatus !== "BANNED");

    const subtotal = activeProducts.reduce((sum: number, p: Product) => sum + p.price, 0);

    const handleBuyAll = async (promoCode?: string) => {
        const ids = activeProducts.map((p: Product) => p.id);
        if (!ids.length) return;
        if (promoCode !== undefined) {
            try {
                window.location.href = await CreateBuyUrl(ids, true, promoCode || undefined);
            } catch (err) {
                console.error("Failed to create payment URL:", err);
            }
        } else {
            setShowPromoModal(true);
        }
    };

    const handlePromoConfirm = async (promoCode?: string) => {
        setShowPromoModal(false);
        const ids = activeProducts.map((p: Product) => p.id);
        try {
            window.location.href = await CreateBuyUrl(ids, true, promoCode);
        } catch (err) {
            console.error("Failed to create payment URL:", err);
        }
    };

    return (
        <>
            {bannedItems.length > 0 && showBannedAlert && (
                <BannedItemsAlert
                    items={bannedItems}
                    context="cart"
                    onDismiss={() => setShowBannedAlert(false)}
                />
            )}

            <CartView
                products={cart.products}
                onRemove={removeFromCart}
                buyAll={handleBuyAll}
            />

            {showPromoModal && (
                <PromoCodeModal
                    subtotal={subtotal}
                    confirmLabel="Checkout"
                    onConfirm={handlePromoConfirm}
                    onClose={() => setShowPromoModal(false)}
                />
            )}
        </>
    );
};

export default CartPage;
