import React from "react";
import { Message } from "../../types/Message/Message";
import { formatMessageTime } from "../../utils/formatMessageTime";
import "./MessageBubble.scss";

interface Props {
    message: Message;
    currentUserId: string;
    partnerUsername: string;
    deleteMessage: (id: string) => void;
}

const MessageBubble: React.FC<Props> = ({ message, currentUserId, partnerUsername, deleteMessage }) => {
    const isMine = message.senderId === currentUserId;

    return (
        <div className={`message${isMine ? " message--mine" : ""}`}>
            <span className="message__sender">{isMine ? "You" : partnerUsername}</span>

            <div className="message__body">{message.text}</div>

            <div className="message__meta">
                {isMine ? (
                    <button className="message__delete" onClick={() => deleteMessage(message.id)}>
                        Delete
                    </button>
                ) : <span />}

                <span className="message__time">
                    {formatMessageTime(message.createdAt)}
                </span>
            </div>
        </div>
    );
};

export default MessageBubble;
