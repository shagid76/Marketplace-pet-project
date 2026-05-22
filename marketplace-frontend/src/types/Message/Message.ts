export type Message = {
    id: string;
    chatId: string;
    senderId: string;
    text: string;
    createdAt: string;
    deleted?: boolean;
};