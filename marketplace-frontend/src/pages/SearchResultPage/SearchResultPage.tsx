import React, { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import SearchResultView from "../../components/SearchResultView/SearchResultView";
import { Product } from "../../types/Product/Product";
import { searchProducts } from "../../services/productService";

export type SortOrder = "none" | "asc" | "desc";

const SearchResultPage: React.FC = () => {
    const location = useLocation();
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState("");
    const [category, setCategory] = useState("");
    const [minPrice, setMinPrice] = useState<string>("");
    const [maxPrice, setMaxPrice] = useState<string>("");
    const [sortOrder, setSortOrder] = useState<SortOrder>("none");

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);

                const params = new URLSearchParams(location.search);

                const query = params.get("query") || "";
                const categoryParam = params.get("category") || "";
                const minPriceParam = params.get("minPrice") || "";
                const maxPriceParam = params.get("maxPrice") || "";

                setSearchQuery(query);
                setCategory(categoryParam);
                setMinPrice(minPriceParam);
                setMaxPrice(maxPriceParam);
                setSortOrder("none");

                if (!query.trim() && !categoryParam.trim() && !minPriceParam.trim() && !maxPriceParam.trim()) {
                    setProducts([]);
                    return;
                }

                const data = await searchProducts({
                    query,
                    category: categoryParam || undefined,
                    minPrice: minPriceParam ? Number(minPriceParam) : undefined,
                    maxPrice: maxPriceParam ? Number(maxPriceParam) : undefined,
                });

                setProducts(data);
            } catch (err) {
                console.error("Error loading search results:", err);
                setProducts([]);
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [location.search]);

    const sortedProducts = useMemo(() => {
        if (sortOrder === "none") return products;
        return [...products].sort((a, b) =>
            sortOrder === "asc" ? a.price - b.price : b.price - a.price
        );
    }, [products, sortOrder]);

    return (
        <SearchResultView
            products={sortedProducts}
            loading={loading}
            searchQuery={searchQuery}
            category={category}
            minPrice={minPrice}
            maxPrice={maxPrice}
            sortOrder={sortOrder}
            onSortChange={setSortOrder}
        />
    );
};

export default SearchResultPage;
