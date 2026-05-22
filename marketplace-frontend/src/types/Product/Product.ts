export type ProductStatus = "ACTIVE" | "BANNED" | "PENDING" | "REJECTED";

export interface Product {
    id: string;
    title: string;
    description: string;
    price: number;
    images: string[];
    category: string;
    modifiedAt: string;
    createdAt: string;
    inStock: boolean;
    productStatus: ProductStatus;
    author: string;
    authorAvatarUrl: string;
}
