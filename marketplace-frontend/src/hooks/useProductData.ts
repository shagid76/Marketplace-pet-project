import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getProductById } from "../services/productService";
import { getCart, addProductToCart, removeProductFromCart } from "../services/cartService";
import { getMe, addProductToWishList, removeProductFromWishList } from "../services/userService";
import { getUserById } from "../services/userService";
import { Product } from "../types/Product/Product";

type PageError =
    | { kind: "banned"; message: string }
    | { kind: "not_found" }
    | { kind: "unknown" };

type ProductQueryData = {
    product: Product | null;
    isInCart: boolean;
    isInWishlist: boolean;
    pageError: PageError | null;
};

async function fetchProductData(id: string): Promise<ProductQueryData> {
    try {
        const product = await getProductById(id);
        let isInCart = false;
        let isInWishlist = false;

        const token = localStorage.getItem("accessToken");
        if (token) {
            try {
                const [cartData, meData] = await Promise.all([getCart(), getMe()]);
                const cartProducts = cartData.products || cartData;
                isInCart = cartProducts.some((item: Product) => item.id === id);
                const wishlistProducts = meData.wishlist || [];
                isInWishlist = wishlistProducts.some((item: string | Product) =>
                    typeof item === "string" ? item === id : item.id === id
                );
            } catch {
                // cart/wishlist unavailable — not fatal
            }
        }

        return { product, isInCart, isInWishlist, pageError: null };
    } catch (err: unknown) {
        const status = (err as { response?: { status?: number } })?.response?.status;
        const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
        if (status === 403 && message) return { product: null, isInCart: false, isInWishlist: false, pageError: { kind: "banned", message } };
        if (status === 404) return { product: null, isInCart: false, isInWishlist: false, pageError: { kind: "not_found" } };
        return { product: null, isInCart: false, isInWishlist: false, pageError: { kind: "unknown" } };
    }
}

export const useProductData = (id: string | undefined) => {
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ["product", id],
        queryFn: () => fetchProductData(id!),
        enabled: !!id,
    });

    const { data: authorData } = useQuery({
        queryKey: ["productAuthor", data?.product?.author],
        queryFn: () => getUserById(data!.product!.author),
        enabled: !!data?.product?.author,
    });

    const addToCartMutation = useMutation({
        mutationFn: (productId: string) => addProductToCart(productId),
        onSuccess: () => {
            queryClient.setQueryData(["product", id], (old: ProductQueryData | undefined) =>
                old ? { ...old, isInCart: true } : old
            );
            queryClient.invalidateQueries({ queryKey: ["cart"] });
        },
    });

    const removeFromCartMutation = useMutation({
        mutationFn: (productId: string) => removeProductFromCart(productId),
        onSuccess: () => {
            queryClient.setQueryData(["product", id], (old: ProductQueryData | undefined) =>
                old ? { ...old, isInCart: false } : old
            );
            queryClient.invalidateQueries({ queryKey: ["cart"] });
        },
    });

    const addToWishlistMutation = useMutation({
        mutationFn: (productId: string) => addProductToWishList(productId),
        onSuccess: () => {
            queryClient.setQueryData(["product", id], (old: ProductQueryData | undefined) =>
                old ? { ...old, isInWishlist: true } : old
            );
            queryClient.invalidateQueries({ queryKey: ["profile"] });
        },
    });

    const removeFromWishlistMutation = useMutation({
        mutationFn: (productId: string) => removeProductFromWishList(productId),
        onSuccess: () => {
            queryClient.setQueryData(["product", id], (old: ProductQueryData | undefined) =>
                old ? { ...old, isInWishlist: false } : old
            );
            queryClient.invalidateQueries({ queryKey: ["profile"] });
        },
    });

    return {
        product: data?.product ?? null,
        isInCart: data?.isInCart ?? false,
        isInWishlist: data?.isInWishlist ?? false,
        pageError: data?.pageError ?? null,
        authorName: authorData?.username ?? null,
        authorAvatar: authorData?.avatar ?? null,
        isLoading,
        addToCart: addToCartMutation.mutateAsync,
        addingToCart: addToCartMutation.isPending,
        removeFromCart: removeFromCartMutation.mutateAsync,
        removingFromCart: removeFromCartMutation.isPending,
        addToWishlist: addToWishlistMutation.mutateAsync,
        addingToWishlist: addToWishlistMutation.isPending,
        removeFromWishlist: removeFromWishlistMutation.mutateAsync,
        removingFromWishlist: removeFromWishlistMutation.isPending,
        cartError: addToCartMutation.error,
    };
};
