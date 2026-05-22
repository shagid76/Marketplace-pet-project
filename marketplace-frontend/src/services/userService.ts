import api from "../api/axiosInstance";
import { User } from "../types/User/User";
import { UserUpdateFormValues } from "../validation/userUpdateSchema";
import { PageResponse } from "../types/Pagination/PageResponse";


export const getMe = async (): Promise<User> => {
    const response = await api.get("/users/me");
    return response.data;
}

export const getUserById = async (targetId: string): Promise<User> => {
    const response = await api.get(`/users/${targetId}`);
    return response.data;
}

export const update = async (data: UserUpdateFormValues): Promise<User> => {
    const formData = new FormData();
    formData.append("username", data.username);
    if (data.avatar) {
        formData.append("avatar", data.avatar);
    }
    formData.append("removeAvatar", String(data.removeAvatar ?? false));

    const response = await api.post(`/users/me`, formData);
    return response.data;
}

export const getAllUsers = async (page: number, size: number): Promise<PageResponse<User>> => {
    const res = await api.get<PageResponse<User>>(`/users?page=${page}&size=${size}`);
    return res.data;
}

export const addProductToWishList = async (productId: string): Promise<void> => {
    await api.patch(`/users/wishlist/${productId}`);
};

export const removeProductFromWishList = async (productId: string): Promise<void> => {
    await api.delete(`/users/wishlist/${productId}`);
};

export const searchUsers = async (username: string): Promise<User[]> => {
    const res = await api.get<User[]>(`/users/search?username=${encodeURIComponent(username)}`);
    return res.data;
};