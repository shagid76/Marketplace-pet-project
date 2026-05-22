import React from "react";
import { useNavigate } from "react-router-dom";
import MessageBubble from "../MessageBubble/MessageBubble";
import { Message } from "../../types/Message/Message";
import "./ChatView.scss";

interface Props {
    messages: Message[];
    text: string;
    setText: (value: string) => void;
    onSend: () => void;
    bottomRef: React.RefObject<HTMLDivElement | null>;
    currentUserId: string;
    partnerUsername: string;
    partnerAvatar: string | null;
    partnerId?: string | null;
    deleteMessage: (id: string) => void;
    onBack?: () => void;
}

const ChatView: React.FC<Props> = ({
    messages, text, setText, onSend, bottomRef, currentUserId,
    partnerUsername, partnerAvatar, partnerId, deleteMessage, onBack
}) => {
    const navigate = useNavigate();
    const initial = partnerUsername?.[0]?.toUpperCase() || "?";

    return (
        <div className="chat">
            <div className="chat__header">
                {onBack && (
                    <button className="chat__header-back" onClick={onBack} aria-label="Back to chats">
                        ←
                    </button>
                )}
                <button
                    className="chat__header-profile"
                    onClick={() => partnerId && navigate(`/user/${partnerId}`)}
                    disabled={!partnerId}
                    aria-label={`View ${partnerUsername}'s profile`}
                >
                    <div className="chat__header-avatar">
                        {partnerAvatar ? (
                            <img
                                src={partnerAvatar}
                                alt={`${partnerUsername}'s avatar`}
                                style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: "50%" }}
                            />
                        ) : (
                            initial
                        )}
                    </div>
                    <h1>{partnerUsername}</h1>
                </button>
            </div>

            <div className="chat__messages">
                {messages.length === 0 ? (
                    <div className="chat__empty">No messages yet — say hi!</div>
                ) : (
                    messages.map((message) => (
                        <MessageBubble
                            key={message.id}
                            message={message}
                            currentUserId={currentUserId}
                            partnerUsername={partnerUsername}
                            deleteMessage={deleteMessage}
                        />
                    ))
                )}
                <div ref={bottomRef} />
            </div>

            <div className="chat__composer">
                <input
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") onSend();
                    }}
                    placeholder="Type a message…"
                />
                <button className="btn" onClick={onSend} disabled={!text.trim()}>
                    Send
                </button>
            </div>
        </div>
    );
};

export default ChatView;
