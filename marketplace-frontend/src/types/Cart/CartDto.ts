import { Product } from "../Product/Product";

export type CartDto = {
    products: Product[];
    totalPrice: number;
}