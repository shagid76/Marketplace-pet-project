import { useQuery } from "@tanstack/react-query";
import { getUserById } from "../services/userService";
import { getAllProductsByAuthorId } from "../services/productService";
import { getAverageRating } from "../services/reviewService";

async function fetchUserData(id: string) {
    const [userResult, productsResult, ratingResult] = await Promise.allSettled([
        getUserById(id),
        getAllProductsByAuthorId(id),
        getAverageRating(id),
    ]);

    return {
        user: userResult.status === "fulfilled" ? userResult.value : null,
        products: productsResult.status === "fulfilled" ? (productsResult.value || []) : [],
        averageRating: ratingResult.status === "fulfilled" ? (ratingResult.value ?? null) : null,
    };
}

export const useUserData = (id: string | undefined) => {
    const { data, isLoading } = useQuery({
        queryKey: ["user", id],
        queryFn: () => fetchUserData(id!),
        enabled: !!id,
    });

    return {
        user: data?.user ?? null,
        products: data?.products ?? [],
        averageRating: data?.averageRating ?? null,
        isLoading,
    };
};
