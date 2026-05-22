import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getCart, removeProductFromCart } from "../services/cartService";

export const useCartData = () => {
    const queryClient = useQueryClient();

    const { data: cart, isLoading } = useQuery({
        queryKey: ["cart"],
        queryFn: getCart,
    });

    const removeMutation = useMutation({
        mutationFn: (productId: string) => removeProductFromCart(productId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["cart"] }),
    });

    return {
        cart: cart ?? null,
        isLoading,
        removeFromCart: removeMutation.mutate,
        removeError: removeMutation.error,
    };
};
