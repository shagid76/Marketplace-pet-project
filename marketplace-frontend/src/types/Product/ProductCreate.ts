export interface ProductCreate{
    title: string;
    description: string;
    price: number;
    images?: File[];
    category: string;
}