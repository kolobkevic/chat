package ru.kolobkevic.chat.chat_server.auth;

import ru.kolobkevic.chat.chat_server.entity.User;

public interface AuthService {
    void start();

    void stop();

    String authorizeUserByLoginAndPassword(String login, String password);

    String changeNickname(String login, String newNickname);

    User createNewUser(String login, String password, String nickname);

    void deleteUser(String login, String password);

    void changePassword(String login, String oldPassword, String newPassword);

    void resetPassword(String login, String newPassword, String secretPhrase);
}
