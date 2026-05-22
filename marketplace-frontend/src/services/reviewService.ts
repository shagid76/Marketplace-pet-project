import api from "../api/axiosInstance";
import { Review } from "../types/Review/Review";
import { ReviewCreate } from "../types/Review/ReviewCreate";
import { PageResponse } from "../types/Pagination/PageResponse";

export const createOrUpdate = async (data: ReviewCreate): Promise<Review> => {
    const res = await api.post<Review>('/reviews', data)
    return res.data;
}

export const getAverageRating = async (id: string): Promise<number> => {
    const res = await api.get<number>(`/reviews/average/${id}`)
    return res.data;
}

export const getMyAverageRating = async (): Promise<number> => {
    const res = await api.get<number>(`/reviews/average/me`)
    return res.data;
}

export const getMyReview = async (targetId: string): Promise<{ description: string; rating: number } | null> => {
    const res = await api.get(`/reviews/my-review`, {
        params: { targetId }
    })
    return res.data;
}

export const getAllReviews = async (page: number, size: number): Promise<PageResponse<Review>> => {
    const res = await api.get<PageResponse<Review>>(`/reviews?page=${page}&size=${size}`)
    return res.data;
}

export const deleteReview = async (id: string): Promise<void> => {
    await api.delete(`/reviews/${id}`);
}

export const getMyReviewWithId = async (targetId: string): Promise<{ id: string; description: string; rating: number } | null> => {
    const res = await api.get(`/reviews/my-review`, { params: { targetId } });
    return res.data;
}