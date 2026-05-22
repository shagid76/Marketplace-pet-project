import api from "../api/axiosInstance";
import { PageResponse } from "../types/Pagination/PageResponse";

export type PromoCodeType = "PERCENTAGE" | "FIXED_AMOUNT";

export interface CreatePromoCodeRequest {
    code: string;
    promoCodeType: PromoCodeType;
    startAt: string;
    endAt: string;
    discountValue: number;
    maxUsagePerUser: number;
    applicableCategories: string[];
    requiredProducts: number;
    requiredPrice: number;
}

export interface PromoCodeDto {
    id: string;
    code: string;
    promoCodeType: PromoCodeType;
    discountValue: number;
    startAt: string;
    endAt: string;
    applicableCategories: string[];
    maxUsagePerUser: number;
    requiredProducts: number;
    requiredPrice: number;
    active: boolean;
}

export const create = async (request: CreatePromoCodeRequest): Promise<PromoCodeDto> => {
    const res = await api.post("/promo_codes", request);
    return res.data;
};

export const getAll = async (page: number, size: number): Promise<PageResponse<PromoCodeDto>> => {
    const res = await api.get<PageResponse<PromoCodeDto>>(`/promo_codes?page=${page}&size=${size}`);
    return res.data;
};

export const getByCode = async (code: string): Promise<PromoCodeDto> => {
    const res = await api.get("/promo_codes/" + code);
    return res.data;
};

export const checkByCode = async (code: string, products: string[]): Promise<PromoCodeDto> => {
    const res = await api.post("/promo_codes/check", {
        code,
        productIds: products,
    });
    return res.data;
};


export const deactivate = async (id: string): Promise<PromoCodeDto> => {
    const res = await api.patch("/promo_codes/" + id + "/deactivate");
    return res.data;
};


