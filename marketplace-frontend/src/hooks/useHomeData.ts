import { useQuery } from "@tanstack/react-query";
import { getNewProducts, getAllCategories, get10ProductsByCategory } from "../services/productService";

const DEFAULT_CATEGORY = "ELECTRONICS";

async function fetchInitialHomeData() {
    const [newProds, categoryList, categoryProducts] = await Promise.all([
        getNewProducts(),
        getAllCategories(),
        get10ProductsByCategory(DEFAULT_CATEGORY),
    ]);
    return { newProducts: newProds, categories: categoryList, initialCategoryProducts: categoryProducts };
}

export const useHomeData = (selectedCategory: string) => {
    const { data: initial, isLoading } = useQuery({
        queryKey: ["homeInitial"],
        queryFn: fetchInitialHomeData,
        staleTime: 60_000,
    });

    const { data: categoryProducts, isFetching: categoryLoading } = useQuery({
        queryKey: ["categoryProducts", selectedCategory],
        queryFn: () => get10ProductsByCategory(selectedCategory),
        enabled: !!selectedCategory,
        staleTime: 30_000,
    });

    return {
        newProducts: initial?.newProducts ?? [],
        categories: initial?.categories ?? [],
        productsByCategory: categoryProducts ?? initial?.initialCategoryProducts ?? [],
        isLoading,
        categoryLoading,
    };
};
