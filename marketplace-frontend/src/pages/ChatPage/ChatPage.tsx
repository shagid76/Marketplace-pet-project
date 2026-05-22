import React, { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getMyChats, getChat, create } from "../../services/chatService";
import { connectChatSocket, disconnectChatSocket, sendMessageWS } from "../../ws/chatSocket";
import ChatListView from "../../components/ChatListView/ChatListView";
import ConfirmModal from "../../components/ConfirmModal/ConfirmModal";
import ChatView from "../../components/ChatView/ChatView";
import { ChatPreview } from "../../types/Chat/ChatPreview";
import { Chat } from "../../types/Chat/Chat";
import { Message } from "../../types/Message/Message";
import { getCurrentUserId } from "../../services/authService";
import { getUserById } from "../../services/userService";
import { remove } from "../../services/messageService";



const ChatPage: React.FC = () => {
    const { chatId } = useParams<{ chatId: string }>();
    const navigate = useNavigate();

    const [chats, setChats] = useState<ChatPreview[]>([]);
    const [chat, setChat] = useState<Chat | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [text, setText] = useState("");
    const [chatUsernames, setChatUsernames] = useState<Record<string, string>>({});
    const [chatAvatars, setChatAvatars] = useState<Record<string, string>>({});
    const [loadingChats, setLoadingChats] = useState(true);
    const [loadingChat, setLoadingChat] = useState(false);
    const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);

    const bottomRef = useRef<HTMLDivElement | null>(null);
    const currentUserId = getCurrentUserId() || "";

    const getOtherUserId = useCallback((chat: ChatPreview) =>
        chat.user1Id === currentUserId ? chat.user2Id : chat.user1Id,
    [currentUserId]);

    useEffect(() => {
        const loadChats = async () => {
            try {
                const data = await getMyChats();
                setChats(data || []);
            } catch (e) {
                console.error("Failed to load chats", e);
            } finally {
                setLoadingChats(false);
            }
        };
        loadChats();
    }, []);

    useEffect(() => {
        if (!chatId) {
            setChat(null);
            setMessages([]);
            return;
        }
        const loadChat = async () => {
            try {
                setLoadingChat(true);
                const data = await getChat(chatId);
                setChat(data);
                setMessages(data.messages || []);
            } catch (e) {
                console.error("Failed to load chat", e);
                setChat(null);
                setMessages([]);
            } finally {
                setLoadingChat(false);
            }
        };
        loadChat();
    }, [chatId]);

    const usersCacheRef = useRef<Record<string, { username: string; avatar: string }>>({});

    useEffect(() => {
        let cancelled = false;
        const loadUsernames = async () => {
            const uniqueUserIds = Array.from(new Set(chats.map(getOtherUserId)));
            const uncachedIds = uniqueUserIds.filter((id) => !usersCacheRef.current[id]);

            try {
                if (uncachedIds.length > 0) {
                    const users = await Promise.all(uncachedIds.map((id) => getUserById(id)));
                    if (cancelled) return;
                    users.forEach((user) => {
                        usersCacheRef.current[user.id] = { username: user.username, avatar: user.avatar };
                    });
                }
                if (cancelled) return;

                const nameMap: Record<string, string> = {};
                const avatarMap: Record<string, string> = {};
                uniqueUserIds.forEach((id) => {
                    const cached = usersCacheRef.current[id];
                    if (cached) {
                        nameMap[id] = cached.username;
                        avatarMap[id] = cached.avatar;
                    }
                });
                setChatUsernames(nameMap);
                setChatAvatars(avatarMap);
            } catch (err) {
                console.error("Failed to load users:", err);
            }
        };

        if (chats.length > 0) loadUsernames();
        return () => {
            cancelled = true;
        };
    }, [chats, getOtherUserId]);

    useEffect(() => {
        if (!chatId) return;
        const token = localStorage.getItem("accessToken");
        if (!token) return;

        connectChatSocket(chatId, token, (event: Message) => {
            if (event.deleted) {
                setMessages((prev) => prev.filter((m) => m.id !== event.id));
            } else {
                setMessages((prev) => {
                    const exists = prev.some((m) => m.id === event.id);
                    if (exists) return prev;
                    return [...prev, event];
                });
            }
        });

        return () => disconnectChatSocket();
    }, [chatId]);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const handleOpenChat = (id: string) => navigate(`/chat/${id}`);

    const handleStartChat = async (userId: string) => {
        try {
            const newChat = await create({ user2Id: userId });
            const updated = await getMyChats();
            setChats(updated || []);
            navigate(`/chat/${newChat.id}`);
        } catch (err) {
            console.error("Failed to start chat:", err);
        }
    };

    const handleSend = () => {
        if (!chatId || !text.trim()) return;
        sendMessageWS(chatId, text);
        setText("");
    };

    const handleDeleteMessage = (id: string) => setPendingDeleteId(id);

    const handleDeleteConfirmed = async () => {
        if (!pendingDeleteId) return;
        const id = pendingDeleteId;
        setPendingDeleteId(null);
        try {
            await remove(id);
            setMessages((prev) => prev.filter((msg) => msg.id !== id));
        } catch (error) {
            console.error("Error deleting message:", error);
        }
    };

    const currentOtherUserId = chat
        ? chat.user1Id === currentUserId ? chat.user2Id : chat.user1Id
        : null;
    const currentChatPartnerName = currentOtherUserId
        ? chatUsernames[currentOtherUserId]
        : "Loading...";

    return (
        <div className="chat-shell">
            <aside className="chat-shell__sidebar">
                {loadingChats ? (
                    <div className="loading-state">Loading chats…</div>
                ) : (
                    <ChatListView
                        chats={chats}
                        onOpenChat={handleOpenChat}
                        onStartChat={handleStartChat}
                        selectedChatId={chatId}
                        chatUsernames={chatUsernames}
                        currentUserId={currentUserId}
                    />
                )}
            </aside>

            <div className="chat-shell__main">
                {!chatId ? (
                    <div className="chat-shell__placeholder">
                        <h3>Select a chat</h3>
                        <p>Pick a conversation from the left to start messaging.</p>
                    </div>
                ) : loadingChat ? (
                    <div className="loading-state">Loading chat…</div>
                ) : chat ? (
                    <ChatView
                        messages={messages}
                        text={text}
                        setText={setText}
                        onSend={handleSend}
                        bottomRef={bottomRef}
                        currentUserId={currentUserId}
                        partnerUsername={currentChatPartnerName}
                        partnerAvatar={currentOtherUserId ? chatAvatars[currentOtherUserId] ?? null : null}
                        partnerId={currentOtherUserId}
                        deleteMessage={handleDeleteMessage}
                    />
                ) : (
                    <div className="chat-shell__placeholder">
                        <h3>Chat not found</h3>
                    </div>
                )}
            </div>

            {pendingDeleteId && (
                <ConfirmModal
                    message="Delete this message? This cannot be undone."
                    confirmLabel="Delete"
                    danger
                    onConfirm={handleDeleteConfirmed}
                    onCancel={() => setPendingDeleteId(null)}
                />
            )}
        </div>
    );
};

export default ChatPage;
