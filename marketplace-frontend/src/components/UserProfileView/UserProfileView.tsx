import { useState } from "react";
import { Product } from "../../types/Product/Product";
import { User } from "../../types/User/User";
import { Link } from "react-router-dom";
import Pagination from "../Pagination/Pagination";
import "./UserProfileView.scss";

const PAGE_SIZE = 5;

interface Props {
    user: User;
    products: Product[];
    isOwner: boolean;
    averageRating: number | null;
    myPurchases?: Product[];
    logout?: () => void;
    removeFromWishlist?: (productId: string) => void;
    moveToCart?: (productId: string) => void;
}

const ProductRow = ({ product, action }: { product: Product; action?: React.ReactNode }) => (
    <li className="profile-list-item">
        {product.images?.[0] && (
            <img
                className="profile-list-item__thumb"
                src={product.images[0]}
                alt={product.title}
            />
        )}
        <Link to={`/product/${product.id}`} className="profile-list-item__title">
            {product.title}
            {!product.inStock && <span className="profile-list-item__sold"> (Sold)</span>}
        </Link>
        <span className="profile-list-item__price">${product.price}</span>
        {action}
    </li>
);

const WishlistRow = ({
    product,
    removeFromWishlist,
    moveToCart,
}: {
    product: Product;
    removeFromWishlist?: (id: string) => void;
    moveToCart?: (id: string) => void;
}) => {
    const isBanned = product.productStatus === "BANNED";

    return (
        <li className={`profile-list-item${isBanned ? " profile-list-item--banned" : ""}`}>
            {product.images?.[0] && (
                <img
                    className="profile-list-item__thumb"
                    src={product.images[0]}
                    alt={product.title}
                />
            )}
            <div className="profile-list-item__info">
                <Link to={`/product/${product.id}`}>
                    {product.title}
                    {!product.inStock && !isBanned && (
                        <span className="profile-list-item__sold"> (Sold)</span>
                    )}
                    {isBanned && (
                        <span className="profile-list-item__banned-badge">Banned</span>
                    )}
                </Link>
                <span className={`profile-list-item__price${isBanned ? " profile-list-item__price--banned" : ""}`}>
                    ${product.price}
                </span>
            </div>

            <div className="profile-list-item__actions">
                {!isBanned && product.inStock && (
                    <button
                        className="btn btn--secondary btn--sm"
                        onClick={() => moveToCart?.(product.id)}
                    >
                        Add to cart
                    </button>
                )}
                <button
                    className="btn btn--ghost btn--sm"
                    onClick={() => removeFromWishlist?.(product.id)}
                >
                    Remove
                </button>
            </div>
        </li>
    );
};

// Generic paginated list section — manages its own page state
function PagedSection<T>({
    title,
    items,
    renderItem,
    emptyText,
    hidden,
}: {
    title: string;
    items: T[];
    renderItem: (item: T) => React.ReactNode;
    emptyText: string;
    hidden?: boolean;
}) {
    const [page, setPage] = useState(0);

    if (hidden) return null;

    const totalPages = Math.ceil(items.length / PAGE_SIZE);
    const paged = items.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

    return (
        <section className="profile__section">
            <h3>{title}</h3>
            {items.length === 0 ? (
                <p className="u-text-muted">{emptyText}</p>
            ) : (
                <>
                    <ul className="profile-list">
                        {paged.map(renderItem)}
                    </ul>
                    <Pagination page={page} totalPages={totalPages} onChange={setPage} />
                </>
            )}
        </section>
    );
}

const UserProfileView = ({
    user, products, isOwner, averageRating, logout, removeFromWishlist, moveToCart, myPurchases = [],
}: Props) => {
    const activeProducts = products.filter(p => p.inStock && p.productStatus !== "BANNED");
    const soldProducts = products.filter(p => !p.inStock);
    const wishlist: Product[] = user.wishlist || [];

    return (
        <div className="profile">
            <aside className="profile__header">
                <img src={user.avatar} alt={`${user.username} avatar`} />
                <h1>{user.username}</h1>
                <p>Joined {new Date(user.createdAt).toLocaleDateString()}</p>

                <span className={`profile__rating${!averageRating || averageRating <= 0 ? " profile__rating--empty" : ""}`}>
                    {averageRating && averageRating > 0
                        ? `★ ${averageRating.toFixed(1)} rating`
                        : "No reviews yet"}
                </span>

                {isOwner && (
                    <div className="profile__owner-actions">
                        <Link to="/create-product"><button className="btn btn--block">Create product</button></Link>
                        <Link to="/update"><button className="btn btn--secondary btn--block">Edit profile</button></Link>
                        {logout && (
                            <button className="btn btn--ghost btn--block" onClick={logout}>Logout</button>
                        )}
                    </div>
                )}
            </aside>

            <div className="profile__main">
                <PagedSection
                    title={isOwner ? "Your products" : `${user.username}'s products`}
                    items={activeProducts}
                    renderItem={(product) => <ProductRow key={product.id} product={product} />}
                    emptyText="No products yet"
                />

                <PagedSection
                    title={isOwner ? "Sold" : `${user.username}'s sold items`}
                    items={soldProducts}
                    renderItem={(product) => <ProductRow key={product.id} product={product} />}
                    emptyText="No sold items yet"
                    hidden={soldProducts.length === 0}
                />

                <PagedSection
                    title="My purchases"
                    items={myPurchases}
                    renderItem={(product) => <ProductRow key={product.id} product={product} />}
                    emptyText="You haven't bought anything yet."
                    hidden={!isOwner}
                />

                <PagedSection
                    title="Wishlist"
                    items={wishlist}
                    renderItem={(product) => (
                        <WishlistRow
                            key={product.id}
                            product={product}
                            removeFromWishlist={removeFromWishlist}
                            moveToCart={moveToCart}
                        />
                    )}
                    emptyText="No products in wishlist yet"
                    hidden={!isOwner}
                />
            </div>
        </div>
    );
};

export default UserProfileView;
