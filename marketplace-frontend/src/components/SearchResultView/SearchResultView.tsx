import React from "react";
import { Link } from "react-router-dom";
import { Product } from "../../types/Product/Product";
import { formatCategory } from "../../utils/formatCategory";
import { SortOrder } from "../../pages/SearchResultPage/SearchResultPage";
import "./SearchResultView.scss";

interface Props {
    products: Product[];
    loading: boolean;
    searchQuery: string;
    category?: string;
    minPrice?: string;
    maxPrice?: string;
    sortOrder: SortOrder;
    onSortChange: (order: SortOrder) => void;
}

const SearchResultView: React.FC<Props> = ({
    products, loading, searchQuery, category, minPrice, maxPrice, sortOrder, onSortChange,
}) => {
    return (
        <div className="search-page">
            <div className="search-page__header">
                <h2>Search results</h2>
                <p className="u-text-muted">
                    {loading
                        ? "Searching the marketplace..."
                        : `${products.length} item${products.length !== 1 ? "s" : ""} found`}
                </p>
            </div>

            <div className="search-page__toolbar">
                <div className="search-page__filters">
                    {searchQuery && (
                        <span className="search-page__filter-pill">
                            <strong>Query:</strong> {searchQuery}
                        </span>
                    )}
                    {category && (
                        <span className="search-page__filter-pill">
                            <strong>Category:</strong> {formatCategory(category)}
                        </span>
                    )}
                    {minPrice && (
                        <span className="search-page__filter-pill">
                            <strong>Min:</strong> ${minPrice}
                        </span>
                    )}
                    {maxPrice && (
                        <span className="search-page__filter-pill">
                            <strong>Max:</strong> ${maxPrice}
                        </span>
                    )}
                </div>

                <div className="search-page__sort">
                    <label htmlFor="sort-select" className="search-page__sort-label">
                        Sort by price:
                    </label>
                    <select
                        id="sort-select"
                        className="search-page__sort-select"
                        value={sortOrder}
                        onChange={(e) => onSortChange(e.target.value as SortOrder)}
                        disabled={loading}
                    >
                        <option value="none">Default</option>
                        <option value="asc">Low to High</option>
                        <option value="desc">High to Low</option>
                    </select>
                </div>
            </div>

            {loading ? (
                <div className="search-page__skeleton-grid">
                    {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className="search-page__skeleton-card" />
                    ))}
                </div>
            ) : products.length > 0 ? (
                <div className="search-page__grid">
                    {products.map((product) => (
                        <Link
                            key={product.id}
                            to={`/product/${product.id}`}
                            className="product-card"
                        >
                            <div className="product-card__media">
                                {product.images && product.images.length > 0 ? (
                                    <img
                                        src={product.images[0]}
                                        alt={product.title}
                                        loading="lazy"
                                    />
                                ) : (
                                    <div className="product-card__placeholder" />
                                )}
                                {!product.inStock && (
                                    <span className="product-card__badge product-card__badge--sold">
                                        Sold
                                    </span>
                                )}
                            </div>
                            <div className="product-card__body">
                                <span className="product-card__category">
                                    {formatCategory(product.category)}
                                </span>
                                <h3 className="product-card__title">{product.title}</h3>
                                <span className="product-card__price">${product.price}</span>
                            </div>
                        </Link>
                    ))}
                </div>
            ) : (
                <div className="search-page__empty">
                    <div className="search-page__empty-icon">&#128269;</div>
                    <h3>No products found</h3>
                    <p>Try a different keyword or remove some filters.</p>
                </div>
            )}
        </div>
    );
};

export default SearchResultView;
