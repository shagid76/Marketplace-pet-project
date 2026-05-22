import { Product } from "../../types/Product/Product";
import { Link, useNavigate } from "react-router-dom";
import { formatCategory } from "../../utils/formatCategory";
import ReportButton from "../Report/ReportButton";
import ProductGallery from "./ProductGallery";
import ProductBuyPanel from "./ProductBuyPanel";
import "./ProductView.scss";

interface Props {
    product: Product;
    isOwner: boolean;
    isAuthenticated: boolean;
    authorName?: string | null;
    authorAvatar?: string | null;
    onDelete: (id: string) => void;
    handleAddToCart: () => void;
    handleRemoveFromCart: () => void;
    isInCart: boolean;
    addingToCart: boolean;
    removingFromCart: boolean;
    isInWishlist: boolean;
    addingToWishlist: boolean;
    removingFromWishlist: boolean;
    handleAddToWishlist: () => void;
    handleRemoveFromWishlist: () => void;
    createBuyUrl: (ids: string[], promoCode?: string) => void;
    onMessageSeller: () => void;
    actionError?: string | null;
    deleteError?: string | null;
}

const ProductView: React.FC<Props> = ({
    product, isOwner, isAuthenticated, authorName, authorAvatar, onDelete,
    handleAddToCart, handleRemoveFromCart, isInCart, addingToCart, removingFromCart,
    isInWishlist, addingToWishlist, removingFromWishlist, handleAddToWishlist, handleRemoveFromWishlist,
    createBuyUrl, onMessageSeller, actionError, deleteError,
}) => {
    const navigate = useNavigate();
    const images = product.images || [];
    const isBanned = product.productStatus === "BANNED";
    const isAvailable = product.inStock && !isBanned;

    return (
        <div className="product-detail">
            <ProductGallery images={images} title={product.title} />

            <div className="product-detail__info">
                <span className="product-detail__category">{formatCategory(product.category)}</span>
                <h1 className="product-detail__title">{product.title}</h1>

                {isBanned && (
                    <div className="product-detail__banned">
                        This product has been banned and is no longer available.
                    </div>
                )}

                {!isBanned && !product.inStock && (
                    <span className="product-detail__sold">Sold / Not in stock</span>
                )}

                {deleteError && (
                    <div className="product-detail__action-error">{deleteError}</div>
                )}

                <div className="product-detail__price">
                    <span>${product.price}</span>
                </div>

                <div className="product-detail__description">{product.description}</div>

                <div className="product-detail__meta">
                    <span>Listed: {new Date(product.createdAt).toLocaleDateString()}</span>
                </div>

                {!isOwner && authorName && (
                    <button
                        className="product-detail__author"
                        onClick={() => navigate(`/user/${product.author}`)}
                        aria-label={`View ${authorName}'s profile`}
                    >
                        {authorAvatar
                            ? <img src={authorAvatar} alt={authorName} className="product-detail__author-avatar" />
                            : <div className="product-detail__author-avatar product-detail__author-avatar--placeholder" />
                        }
                        <div className="product-detail__author-info">
                            <span className="product-detail__author-label">Sold by</span>
                            <span className="product-detail__author-name">{authorName}</span>
                        </div>
                    </button>
                )}

                {isOwner ? (
                    <div className="product-detail__owner-actions">
                        <Link to="/update-product" state={{ productId: product.id }}>
                            <button className="btn btn--secondary btn--block">Edit listing</button>
                        </Link>
                        <button
                            className="btn btn--danger btn--block"
                            onClick={() => onDelete(product.id)}
                        >
                            Delete
                        </button>
                    </div>
                ) : (
                    <>
                        <div className="product-detail__actions">
                            <button
                                className={`btn btn--secondary${addingToCart || removingFromCart ? " btn--loading" : ""}`}
                                onClick={isInCart ? handleRemoveFromCart : handleAddToCart}
                                disabled={addingToCart || removingFromCart || !isAvailable}
                            >
                                {isInCart ? "Remove from Cart" : "Add to Cart"}
                            </button>

                            <button
                                className={`btn btn--ghost${addingToWishlist || removingFromWishlist ? " btn--loading" : ""}`}
                                onClick={isInWishlist ? handleRemoveFromWishlist : handleAddToWishlist}
                                disabled={addingToWishlist || removingFromWishlist}
                            >
                                {isInWishlist ? "♥ In wishlist" : "♡ Wishlist"}
                            </button>

                            <button
                                className="btn btn--ghost"
                                onClick={onMessageSeller}
                            >
                                Message seller
                            </button>
                        </div>

                        {actionError && (
                            <div className="product-detail__action-error">{actionError}</div>
                        )}

                        {product.inStock && (
                            <div className="product-detail__report">
                                <ReportButton
                                    targetType="PRODUCT"
                                    targetId={product.id}
                                    ownerId={product.author}
                                />
                            </div>
                        )}

                        {isAvailable && isAuthenticated && (
                            <ProductBuyPanel
                                productId={product.id}
                                price={product.price}
                                createBuyUrl={createBuyUrl}
                            />
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default ProductView;
