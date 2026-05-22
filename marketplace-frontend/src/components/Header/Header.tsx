import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../hooks/useAuth";
import { getCart } from "../../services/cartService";
import { CartDto } from "../../types/Cart/CartDto";
import { getAllCategories } from "../../services/productService";
import { formatCategory } from "../../utils/formatCategory";
import "./Header.scss";


const Header: React.FC = () => {
    const { isAuthenticated, handleLogout, hasRole } = useAuth();
    const isAdminOrMod = hasRole(["ROLE_ADMIN", "ROLE_MODERATOR"]);
    const navigate = useNavigate();

    const [search, setSearch] = useState("");
    const [category, setCategory] = useState("");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");
    const [categories, setCategories] = useState<string[]>([]);

    const { data: cartData } = useQuery<CartDto>({
        queryKey: ["cart"],
        queryFn: getCart,
        enabled: isAuthenticated ?? false,
    });
    const cartLength = cartData?.products?.length ?? 0;

    const fetchCategories = async () => {
        try {
            const response = await getAllCategories();
            setCategories(response);
        } catch (err) {
            console.error("Failed to load categories", err);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const handleSearch = () => {
        const params = new URLSearchParams();
        if (search.trim()) params.append("query", search.trim());
        if (category) params.append("category", category);
        if (minPrice) params.append("minPrice", minPrice);
        if (maxPrice) params.append("maxPrice", maxPrice);

        navigate(`/search?${params.toString()}`);
    };

    const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") handleSearch();
    };

    return (
        <header className="site-header">
            <div className="site-header__inner">
                <div className="site-header__brand" onClick={() => navigate("/")}>
                    <img src="/logo.png" alt="Logo" />
                    <span className="site-header__brand-text">Marketplace</span>
                </div>

                <div className="site-header__search">
                    <input
                        type="text"
                        placeholder="Search products..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        onKeyDown={handleKeyPress}
                    />
                    <select value={category} onChange={(e) => setCategory(e.target.value)}>
                        <option value="">All categories</option>
                        {categories.map((cat) => (
                            <option key={cat} value={cat}>
                                {formatCategory(cat)}
                            </option>
                        ))}
                    </select>
                    <input
                        type="number"
                        placeholder="Min $"
                        value={minPrice}
                        onChange={(e) => setMinPrice(e.target.value)}
                        min={0}
                    />
                    <input
                        type="number"
                        placeholder="Max $"
                        value={maxPrice}
                        onChange={(e) => setMaxPrice(e.target.value)}
                        min={0}
                    />
                    <button onClick={handleSearch} aria-label="Search">Search</button>
                </div>

                <div className="site-header__actions">
                    {isAuthenticated ? (
                        <>
                            {isAdminOrMod && (
                                <button
                                    className="header-admin-btn u-hidden-mobile"
                                    onClick={() => navigate("/admin-page")}
                                >
                                    Admin panel
                                </button>
                            )}
                            <button onClick={() => navigate("/me")}>Profile</button>
                            <button onClick={() => navigate("/chat")} className="u-hidden-mobile">Chats</button>
                            <button
                                className="cart-badge"
                                onClick={() => navigate("/cart")}
                                aria-label={`Cart (${cartLength})`}
                            >
                                Cart
                                {cartLength > 0 && (
                                    <span className="cart-badge__count">{cartLength}</span>
                                )}
                            </button>
                            <button onClick={handleLogout} className="u-hidden-mobile">Logout</button>
                        </>
                    ) : (
                        <button className="header-cta" onClick={() => navigate("/login")}>
                            Sign in
                        </button>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;
