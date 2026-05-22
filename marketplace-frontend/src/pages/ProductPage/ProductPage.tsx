import React, { useState } from "react";
import ProductView from "../../components/ProductView/ProductView";
import { useParams, useNavigate } from "react-router-dom";
import { deleteProduct } from "../../services/productService";
import { jwtDecode } from "jwt-decode";
import { CreateBuyUrl } from "../../services/paymentService";
import { create as createChat } from "../../services/chatService";
import PromoCodeModal from "../../components/PromoCode/PromoCodeModal";
import ConfirmModal from "../../components/ConfirmModal/ConfirmModal";
import { useProductData } from "../../hooks/useProductData";
import { useQueryClient } from "@tanstack/react-query";

interface DecodedToken {
    userId: string;
}

const ProductPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [showPromoModal, setShowPromoModal] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
    const [pendingBuyIds, setPendingBuyIds] = useState<string[]>([]);
    const [actionError, setActionError] = useState<string | null>(null);
    const [deleteError, setDeleteError] = useState<string | null>(null);

    const {
        product, isLoading, pageError,
        isInCart, isInWishlist,
        authorName, authorAvatar,
        addToCart, addingToCart,
        removeFromCart, removingFromCart,
        addToWishlist, addingToWishlist,
        removeFromWishlist, removingFromWishlist,
    } = useProductData(id);

    const isOwner = (() => {
        const token = localStorage.getItem("accessToken");
        if (!token || !product) return false;
        try {
            return product.author === jwtDecode<DecodedToken>(token).userId;
        } catch {
            return false;
        }
    })();

    const requireAuth = (): boolean => {
        if (!localStorage.getItem("accessToken")) {
            navigate("/login");
            return false;
        }
        return true;
    };

    const handleDelete = (productId: string) => {
        setPendingDeleteId(productId);
        setShowDeleteConfirm(true);
    };

    const handleDeleteConfirmed = async () => {
        if (!pendingDeleteId) return;
        setShowDeleteConfirm(false);
        try {
            setDeleteError(null);
            await deleteProduct(pendingDeleteId);
            await queryClient.invalidateQueries({ queryKey: ["profile"] });
            navigate("/me");
        } catch (err: any) {
            setDeleteError(err?.response?.data?.message || "Failed to delete product");
        } finally {
            setPendingDeleteId(null);
        }
    };

    const handleAddToCart = async () => {
        if (!requireAuth() || !product) return;
        try {
            setActionError(null);
            await addToCart(product.id);
        } catch (err: any) {
            setActionError(err?.response?.data?.message || "This product is no longer available.");
        }
    };

    const handleRemoveFromCart = async () => {
        if (!requireAuth() || !product) return;
        try {
            await removeFromCart(product.id);
        } catch {
            // silent
        }
    };

    const handleAddToWishlist = async () => {
        if (!requireAuth() || !product) return;
        try {
            await addToWishlist(product.id);
        } catch {
            // silent
        }
    };

    const handleRemoveFromWishlist = async () => {
        if (!requireAuth() || !product) return;
        try {
            await removeFromWishlist(product.id);
        } catch {
            // silent
        }
    };

    const handleMessageSeller = async () => {
        if (!requireAuth() || !product) return;
        try {
            const chat = await createChat({ user2Id: product.author });
            navigate(`/chat/${chat.id}`);
        } catch (err: any) {
            setActionError(err?.response?.data?.message || "Could not open chat.");
        }
    };

    const handleCreateBuyUrl = async (ids: string[], promoCode?: string) => {
        if (!requireAuth() || !ids.length) return;
        setActionError(null);
        if (promoCode !== undefined) {
            try {
                window.location.href = await CreateBuyUrl(ids, false, promoCode || undefined);
            } catch (err: any) {
                setActionError(err?.response?.data?.message || "This product is no longer available.");
            }
        } else {
            setPendingBuyIds(ids);
            setShowPromoModal(true);
        }
    };

    const handlePromoConfirm = async (promoCode?: string) => {
        setShowPromoModal(false);
        setActionError(null);
        try {
            window.location.href = await CreateBuyUrl(pendingBuyIds, false, promoCode);
        } catch (err: any) {
            setActionError(err?.response?.data?.message || "This product is no longer available.");
        }
    };

    if (isLoading) return <div className="loading-state">Loading...</div>;

    if (pageError) {
        if (pageError.kind === "banned") {
            return (
                <div className="state-page">
                    <div className="state-page__icon">🚫</div>
                    <h1>Product unavailable</h1>
                    <p>{pageError.message}</p>
                </div>
            );
        }
        if (pageError.kind === "not_found") {
            return <div className="state-page"><h1>Product not found</h1></div>;
        }
        return (
            <div className="state-page">
                <h1>Something went wrong</h1>
                <p>Could not load this product. Please try again later.</p>
            </div>
        );
    }

    if (!product) return <div className="state-page"><h1>Product not found</h1></div>;

    return (
        <>
            <ProductView
                product={product}
                isOwner={isOwner}
                isAuthenticated={!!localStorage.getItem("accessToken")}
                authorName={authorName}
                authorAvatar={authorAvatar}
                onDelete={handleDelete}
                handleAddToCart={handleAddToCart}
                isInCart={isInCart}
                handleRemoveFromCart={handleRemoveFromCart}
                addingToCart={addingToCart}
                removingFromCart={removingFromCart}
                isInWishlist={isInWishlist}
                addingToWishlist={addingToWishlist}
                removingFromWishlist={removingFromWishlist}
                handleAddToWishlist={handleAddToWishlist}
                handleRemoveFromWishlist={handleRemoveFromWishlist}
                createBuyUrl={handleCreateBuyUrl}
                onMessageSeller={handleMessageSeller}
                actionError={actionError}
                deleteError={deleteError}
            />

            {showPromoModal && (
                <PromoCodeModal
                    subtotal={product.price}
                    confirmLabel="Buy now"
                    onConfirm={handlePromoConfirm}
                    onClose={() => setShowPromoModal(false)}
                />
            )}

            {showDeleteConfirm && (
                <ConfirmModal
                    message="Are you sure you want to delete this listing? This action cannot be undone."
                    confirmLabel="Delete"
                    danger
                    onConfirm={handleDeleteConfirmed}
                    onCancel={() => setShowDeleteConfirm(false)}
                />
            )}
        </>
    );
};

export default ProductPage;
