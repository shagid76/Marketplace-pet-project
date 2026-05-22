import { Link } from "react-router-dom";
import { Product } from "../../types/Product/Product";
import { formatCategory } from "../../utils/formatCategory";
import "./HomeView.scss";

interface Props {
    newProducts: Product[];
    productsByCategory: Product[];
    categories: string[];
    selectedCategory: string;
    onCategoryChange: (category: string) => void;
    categoryLoading: boolean;
}

const ProductCard = ({ product }: { product: Product }) => (
    <Link to={`/product/${product.id}`} className="product-card">
        <div className="product-card__media">
            {product.images?.[0] && (
                <img src={product.images[0]} alt={product.title} loading="lazy" />
            )}
            {!product.inStock && <span className="product-card__badge product-card__badge--sold">Sold</span>}
        </div>
        <div className="product-card__body">
            <span className="product-card__category">{formatCategory(product.category)}</span>
            <h3 className="product-card__title">{product.title}</h3>
            <span className="product-card__price">${product.price}</span>
        </div>
    </Link>
);

const HomeView = ({
    newProducts,
    productsByCategory,
    categories,
    selectedCategory,
    onCategoryChange,
    categoryLoading,
}: Props) => {
    return (
        <div className="home">
            <section className="hero">
                <span className="hero__eyebrow">New season • Curated picks</span>
                <h1 className="hero__title">
                    Discover, list and trade<br />
                    products you <em>actually</em> want.
                </h1>
                <p className="hero__subtitle">
                    A modern marketplace for makers and buyers. Browse the latest arrivals,
                    chat with sellers in real time and check out securely with Stripe.
                </p>
            </section>

            <section className="section">
                <div className="section__header">
                    <h2 className="section__title">Latest arrivals</h2>
                </div>
                <div className="product-strip">
                    {newProducts.map((product) => (
                        <ProductCard key={product.id} product={product} />
                    ))}
                </div>
            </section>

            <section className="section">
                <div className="section__header">
                    <h2 className="section__title">Browse by category</h2>
                </div>

                <div className="category-strip">
                    {categories.map((category) => (
                        <button
                            key={category}
                            type="button"
                            className={`category-strip__pill ${
                                category === selectedCategory ? "category-strip__pill--active" : ""
                            }`}
                            onClick={() => onCategoryChange(category)}
                            disabled={categoryLoading}
                        >
                            {formatCategory(category)}
                        </button>
                    ))}
                </div>

                {categoryLoading ? (
                    <div className="loading-state">Loading products…</div>
                ) : (
                    <div className="product-grid">
                        {productsByCategory.map((product) => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
};

export default HomeView;
