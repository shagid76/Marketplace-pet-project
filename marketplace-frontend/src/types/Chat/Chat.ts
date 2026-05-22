import { Message } from "../Message/Message";

export type Chat = {
    id: string;
    user1Id: string;
    user2Id: string;
    createdAt: string;
    updatedAt?: string;
    messages?: Message[];
};