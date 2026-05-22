import api from "../api/axiosInstance";
import { CreateReportPayload } from "../types/Report/CreateReportPayload";
import { Report } from "../types/Report/Report";
import { PageResponse } from "../types/Pagination/PageResponse";

export const create = async (payload: CreateReportPayload): Promise<Report> => {
    const res = await api.post<Report>('/reports/create', payload)
    return res.data;
}

export const getAllActiveReports = async (page: number, size: number): Promise<PageResponse<Report>> => {
    const res = await api.get<PageResponse<Report>>(`/reports/active?page=${page}&size=${size}`)
    return res.data;
}

export const solve = async (id: string): Promise<Report> => {
    const res = await api.patch<Report>(`/reports/solve/${id}`);
    return res.data;
}