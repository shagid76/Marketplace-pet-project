import { Product } from "../Product/Product";

export interface User{
    id: string;
    email: string;
    username: string;
    avatar: string;
    role: string[];
    createdAt: string;
    blockedUntil: string;
    banned: boolean;
    wishlist: Product[]
}