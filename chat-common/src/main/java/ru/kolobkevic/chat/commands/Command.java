package ru.kolobkevic.chat.commands;

public enum Command {
    BROADCAST_MESSAGE("/broadcast"),
    LIST_USERS("/list"),
    PRIVATE_MESSAGE("/private"),
    REMOVE("/remove"),
    ERROR("/error"),
    AUTH_OK("/auth_ok"),
    AUTH("/auth"),
    REGISTER("/register"),
    CHANGE_PASS_OK("/change_pass_ok"),
    CHANGE_NICK_OK("/change_nick_ok");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
