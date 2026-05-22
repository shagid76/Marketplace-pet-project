import React, { useEffect, useRef, useState } from "react";
import { ChatPreview } from "../../types/Chat/ChatPreview";
import { User } from "../../types/User/User";
import { formatChatPreviewTime } from "../../utils/formatChatPreviewTime";
import { searchUsers } from "../../services/userService";
import "./ChatListView.scss";

interface Props {
    chats: ChatPreview[];
    onOpenChat: (chatId: string) => void;
    onStartChat: (userId: string) => void;
    selectedChatId?: string | null;
    chatUsernames: Record<string, string>;
    currentUserId: string;
}

const ChatListView: React.FC<Props> = ({
    chats,
    onOpenChat,
    onStartChat,
    selectedChatId,
    chatUsernames,
    currentUserId,
}) => {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<User[]>([]);
    const [searching, setSearching] = useState(false);
    const [searchError, setSearchError] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    // Debounced search
    useEffect(() => {
        if (!query.trim()) {
            setResults([]);
            setSearchError(false);
            return;
        }
        const timer = setTimeout(async () => {
            setSearching(true);
            setSearchError(false);
            try {
                const users = await searchUsers(query.trim());
                setResults(users.filter((u) => u.id !== currentUserId));
            } catch {
                setSearchError(true);
                setResults([]);
            } finally {
                setSearching(false);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [query, currentUserId]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClick = (e: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setQuery("");
                setResults([]);
            }
        };
        document.addEventListener("mousedown", handleClick);
        return () => document.removeEventListener("mousedown", handleClick);
    }, []);

    const handleSelect = (userId: string) => {
        setQuery("");
        setResults([]);
        onStartChat(userId);
    };

    return (
        <div className="chat-list">
            <h2 className="chat-list__title">My chats</h2>

            <div className="chat-search" ref={dropdownRef}>
                <input
                    className="chat-search__input"
                    type="text"
                    placeholder="Search users to message..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    autoComplete="off"
                />
                {query.trim() && (
                    <div className="chat-search__dropdown">
                        {searching && (
                            <div className="chat-search__status">Searching...</div>
                        )}
                        {!searching && searchError && (
                            <div className="chat-search__status chat-search__status--error">
                                Search failed. Try again.
                            </div>
                        )}
                        {!searching && !searchError && results.length === 0 && (
                            <div className="chat-search__status">No users found.</div>
                        )}
                        {!searching && results.map((user) => (
                            <button
                                key={user.id}
                                className="chat-search__result"
                                onClick={() => handleSelect(user.id)}
                            >
                                {user.avatar ? (
                                    <img
                                        className="chat-search__avatar"
                                        src={user.avatar}
                                        alt={user.username}
                                    />
                                ) : (
                                    <div className="chat-search__avatar chat-search__avatar--placeholder" />
                                )}
                                <span className="chat-search__username">{user.username}</span>
                            </button>
                        ))}
                    </div>
                )}
            </div>

            {chats.length === 0 ? (
                <div className="chat-list__empty">No chats yet</div>
            ) : (
                <ul>
                    {chats.map((chat) => {
                        const isActive = selectedChatId === chat.id;
                        const otherUserId =
                            chat.user1Id === currentUserId ? chat.user2Id : chat.user1Id;

                        const username = chatUsernames[otherUserId] || "Loading...";

                        const senderName =
                            chat.lastMessageSenderId === currentUserId
                                ? "You"
                                : username;

                        const previewText = chat.lastMessageText
                            ? `${senderName}: ${chat.lastMessageText}`
                            : "No messages yet";

                        return (
                            <li key={chat.id}>
                                <button
                                    onClick={() => onOpenChat(chat.id)}
                                    className={`chat-item${isActive ? " chat-item--active" : ""}`}
                                >
                                    <div className="chat-item__title">{username}</div>

                                    <div className="chat-item__meta">
                                        <div className="chat-item__preview">
                                            {previewText}
                                        </div>

                                        {chat.lastMessageTime && (
                                            <span className="chat-item__time">
                                                {formatChatPreviewTime(chat.lastMessageTime)}
                                            </span>
                                        )}
                                    </div>
                                </button>
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
};

export default ChatListView;
