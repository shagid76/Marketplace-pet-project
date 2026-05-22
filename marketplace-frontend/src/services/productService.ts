import { ProductCreate } from "../types/Product/ProductCreate";
import { Product } from "../types/Product/Product";
import api from "../api/axiosInstance";
import { ProductUpdate } from "../types/Product/ProductUpdate";
import { PageResponse } from "../types/Pagination/PageResponse";

type SearchProductsParams = {
    query?: string;
    category?: string;
    minPrice?: number;
    maxPrice?: number;
};


export const createProduct = async (data: ProductCreate): Promise<Product> => {
    const formData = new FormData();
    formData.append(
        "product",
        new Blob([JSON.stringify({
            title: data.title,
            description: data.description,
            price: data.price,
            category: data.category
        })], { type: "application/json" })
    );

    data.images?.forEach((image) => {
        formData.append("images", image);
    });

    const response = await api.post<Product>(`/products`, formData)
    return response.data;
}

export const updateProduct = async (data: ProductUpdate, existingImages: string[], id: string): Promise<Product> => {
    const formData = new FormData();
    formData.append(
        "product",
        new Blob([JSON.stringify({
            title: data.title,
            description: data.description,
            price: data.price,
            category: data.category,
            existingImages: existingImages
        })], { type: "application/json" })
    );

    data.images?.forEach((image) => {
        formData.append("images", image);
    });

    const response = await api.patch<Product>(`/products/${id}`, formData)
    return response.data;
}

export const getAllCategories = async (): Promise<string[]> => {
    const response = await api.get<string[]>(`/products/categories`);
    return response.data;
}

export const getAllMyProducts = async (): Promise<Product[]> => {
    const response = await api.get<Product[]>(`/products/me`);
    return response.data;
}

export const getAllProducts = async (page: number, size: number): Promise<PageResponse<Product>> => {
    const response = await api.get<PageResponse<Product>>(`/products?page=${page}&size=${size}`)
    return response.data;
}

export const getAllProductsByAuthorId = async (authorId: string): Promise<Product[]> => {
    const response = await api.get<Product[]>(`/products/author`, {
        params: {
            targetId: authorId
        }
    }
    );
    return response.data;
}

export const getProductById = async (productId: string): Promise<Product> => {
    const response = await api.get<Product>(`/products/${productId}`)
    return response.data;
}

export const deleteProduct = async (id: string): Promise<void> => {
    try {
        await api.delete(`/products/${id}`);
    } catch (err) {
        console.error(err);
    }
}

export const getNewProducts = async (): Promise<Product[]> => {
    const response = await api.get<Product[]>(`/products/new-products`);
    return response.data;
}

export const get10ProductsByCategory = async (category: string): Promise<Product[]> => {
    const response = await api.get<Product[]>(`/products/category/${category}`);
    return response.data;
}

export const searchProducts = async (params: SearchProductsParams): Promise<Product[]> => {
    const searchParams = new URLSearchParams();

    if (params.query?.trim()) {
        searchParams.append("query", params.query.trim());
    }

    if (params.category?.trim()) {
        searchParams.append("category", params.category.trim());
    }

    if (params.minPrice !== undefined && params.minPrice !== null) {
        searchParams.append("minPrice", String(params.minPrice));
    }

    if (params.maxPrice !== undefined && params.maxPrice !== null) {
        searchParams.append("maxPrice", String(params.maxPrice));
    }

    const response = await api.get<Product[]>(`/products/search?${searchParams.toString()}`);
    return response.data;
};

export const getAllPurchases = async (): Promise<Product[]> => {
    const response = await api.get<Product[]>(`/products/me/purchases`)
    return response.data;
}