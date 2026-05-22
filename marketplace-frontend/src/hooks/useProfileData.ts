import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getMe, removeProductFromWishList } from "../services/userService";
import { getAllMyProducts, getProductById, getAllPurchases } from "../services/productService";
import { getMyAverageRating } from "../services/reviewService";
import { addProductToCart } from "../services/cartService";
import { Product } from "../types/Product/Product";

async function fetchProfileData() {
    const [me, myProducts, myAverageRating, purchases] = await Promise.all([
        getMe(),
        getAllMyProducts(),
        getMyAverageRating(),
        getAllPurchases(),
    ]);

    const wishlistFull = await Promise.allSettled(
        (me.wishlist || []).map((p: Product) => getProductById(p.id))
    );
    const resolvedWishlist = wishlistFull
        .filter((r): r is PromiseFulfilledResult<Product> => r.status === "fulfilled")
        .map((r) => r.value);

    return {
        user: { ...me, wishlist: resolvedWishlist },
        products: (myProducts || []) as Product[],
        averageRating: (myAverageRating ?? null) as number | null,
        purchases: (purchases || []) as Product[],
    };
}

export const useProfileData = () => {
    const queryClient = useQueryClient();

    const { data, isLoading, error } = useQuery({
        queryKey: ["profile"],
        queryFn: fetchProfileData,
    });

    const removeFromWishlistMutation = useMutation({
        mutationFn: (productId: string) => removeProductFromWishList(productId),
        onSuccess: (_, productId) => {
            queryClient.setQueryData(["profile"], (old: typeof data) => {
                if (!old) return old;
                return {
                    ...old,
                    user: {
                        ...old.user,
                        wishlist: old.user.wishlist.filter((p: Product) => p.id !== productId),
                    },
                };
            });
        },
    });

    const moveToCartMutation = useMutation({
        mutationFn: async (productId: string) => {
            await addProductToCart(productId);
            await removeProductFromWishList(productId);
        },
        onSuccess: (_, productId) => {
            queryClient.setQueryData(["profile"], (old: typeof data) => {
                if (!old) return old;
                return {
                    ...old,
                    user: {
                        ...old.user,
                        wishlist: old.user.wishlist.filter((p: Product) => p.id !== productId),
                    },
                };
            });
            queryClient.invalidateQueries({ queryKey: ["cart"] });
        },
    });

    return {
        user: data?.user ?? null,
        products: data?.products ?? [],
        averageRating: data?.averageRating ?? null,
        purchases: data?.purchases ?? [],
        isLoading,
        error: error ? "Failed to load profile" : null,
        removeFromWishlist: removeFromWishlistMutation.mutate,
        moveToCart: moveToCartMutation.mutate,
    };
};
