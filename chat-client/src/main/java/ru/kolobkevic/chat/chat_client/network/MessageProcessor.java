package ru.kolobkevic.chat.chat_client.network;

public interface MessageProcessor {
    void processMessage(String message);
}
