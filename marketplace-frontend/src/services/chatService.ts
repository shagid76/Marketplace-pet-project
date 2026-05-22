import api from "../api/axiosInstance";
import { Chat } from "../types/Chat/Chat";
import { ChatPreview } from "../types/Chat/ChatPreview";
import { CreateChatPayload } from "../types/Chat/CreateChatPayload";

export const create = async (payload: CreateChatPayload): Promise<Chat> => {
    const res = await api.post<Chat>("/chats", payload);
    return res.data;
};

export const getChat = async (chatId: string): Promise<Chat> => {
    const res = await api.get<Chat>(`/chats/${chatId}`);
    return res.data;
};

export const getMyChats = async (): Promise<ChatPreview[]> => {
    const res = await api.get<ChatPreview[]>("/chats");
    return res.data;
};

export const remove = async (chatId: string): Promise<void> => {
    await api.delete(`/chats/${chatId}`);
};