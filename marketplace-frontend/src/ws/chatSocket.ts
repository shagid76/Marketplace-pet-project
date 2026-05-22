import SockJS from "sockjs-client";
import { Client, IMessage } from "@stomp/stompjs";
import { Message } from "../types/Message/Message";
import { API_BASE } from "../config/api";

const isDev = process.env.NODE_ENV === "development";

let stompClient: Client | null = null;

export const connectChatSocket = (
  chatId: string,
  token: string,
  onMessage: (message: Message) => void
) => {
  const socket = new SockJS(`${API_BASE}/ws`);

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    debug: isDev ? (msg) => console.log("[STOMP]", msg) : () => {},
  });

  stompClient.onConnect = () => {
    stompClient?.subscribe(`/topic/chat/${chatId}`, (msg: IMessage) => {
      if (msg.body) {
        const message: Message = JSON.parse(msg.body);
        onMessage(message);
      }
    });
  };

  stompClient.onStompError = (frame) => {
    console.error("[STOMP] Broker error:", frame.headers["message"]);
    console.error("[STOMP] Details:", frame.body);
  };

  stompClient.activate();
};

export const disconnectChatSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
};

export const sendMessageWS = (chatId: string, text: string) => {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: "/app/chat.sendMessage",
      body: JSON.stringify({ chatId, text }),
    });
  } else {
    console.warn("[STOMP] Cannot send — connection not established yet.");
  }
};