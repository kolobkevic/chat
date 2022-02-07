package ru.kolobkevic.chat.chat_server.error;

public class WrongCredentialsExceptions extends RuntimeException{
    public WrongCredentialsExceptions() {
    }

    public WrongCredentialsExceptions(String message) {
        super(message);
    }
}
