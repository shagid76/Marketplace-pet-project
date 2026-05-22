import React from "react";
import UserProfileView from "../../components/UserProfileView/UserProfileView";
import { useAuth } from "../../hooks/useAuth";
import { getCurrentUserId } from "../../services/authService";
import BannedItemsAlert from "../../components/BannedItemsAlert/BannedItemsAlert";
import { useProfileData } from "../../hooks/useProfileData";
import { Product } from "../../types/Product/Product";
import { useState } from "react";

const ProfilePage: React.FC = () => {
    const { handleLogout } = useAuth();
    const currentUserId = getCurrentUserId();
    const [showBannedAlert, setShowBannedAlert] = useState(false);

    const { user, products, averageRating, purchases, isLoading, error, removeFromWishlist, moveToCart } =
        useProfileData();

    if (isLoading) return <div className="loading-state">Loading...</div>;
    if (error) return <div>{error}</div>;
    if (!user) return <div>User not found</div>;

    const bannedWishlistItems = (user.wishlist || [])
        .filter((p: Product) => p.productStatus === "BANNED")
        .map((p: Product) => ({ id: p.id, title: p.title }));

    const hasBanned = bannedWishlistItems.length > 0;

    return (
        <>
            {hasBanned && showBannedAlert === false && (
                // Show alert once on first render when banned items exist
                <BannedItemsAlert
                    items={bannedWishlistItems}
                    context="wishlist"
                    onDismiss={() => setShowBannedAlert(true)}
                />
            )}
            <div>
                <UserProfileView
                    user={user}
                    products={products}
                    isOwner={currentUserId === user.id}
                    averageRating={averageRating}
                    logout={handleLogout}
                    removeFromWishlist={removeFromWishlist}
                    moveToCart={moveToCart}
                    myPurchases={purchases}
                />
            </div>
        </>
    );
};

export default ProfilePage;
