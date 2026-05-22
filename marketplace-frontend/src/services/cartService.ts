import api from "../api/axiosInstance";
import { CartDto } from "../types/Cart/CartDto";

export const getCartLength = async (): Promise<number> => {
    const response = await api.get<number>("/carts/length");
    return response.data;
}

export const getCart = async (): Promise<CartDto> => {
    const response = await api.get("/carts");
    return response.data;
};

export const dispatchCartUpdated = (): void => {
    window.dispatchEvent(new Event("cart-updated"));
};

export const addProductToCart = async (productId: string): Promise<CartDto> => {
    const response = await api.patch(`/carts/add/${productId}`);
    dispatchCartUpdated();
    return response.data;
};

export const removeProductFromCart = async (productId: string): Promise<CartDto> => {
    const response = await api.delete(`/carts/${productId}`);
    dispatchCartUpdated();
    return response.data;
};
