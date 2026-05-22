import api from "../api/axiosInstance";
import { Message } from "../types/Message/Message";
import { CreateMessagePayload } from "../types/Message/CreateMessagePayload";


export const getById = async (id: string): Promise<Message> => {
    const res = await api.get<Message>(`/messages/${id}`);
    return res.data;
};

export const getByChatId = async (chatId: string): Promise<Message[]> => {
    const res = await api.get<Message[]>(`/messages/chat/${chatId}`);
    return res.data;
};

export const create = async (payload: CreateMessagePayload): Promise<Message> => {
    const res = await api.post<Message>("/messages", payload);
    return res.data;
};

export const remove = async (id: string): Promise<void> => {
    await api.delete(`/messages/${id}`);
};