import api from "../api/axiosInstance";

export const CreateBuyUrl = async (id: string[], inCart: boolean, promoCode?: string): Promise<string> => {
    const res = await api.post(`/payments/create`, {
        productId: id,
        inCart: inCart,
    },
    {
        params: promoCode ? { promoCode } : {}
    });
    return res.data;
};
