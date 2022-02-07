package ru.kolobkevic.chat.chat_server.auth;

import ru.kolobkevic.chat.chat_server.entity.User;
import ru.kolobkevic.chat.chat_server.error.WrongCredentialsExceptions;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {
    private List<User> usersList;

    public InMemoryAuthService() {
        this.usersList = new ArrayList<>();
        usersList.addAll(List.of(
                new User("login1", "password", "secret", "nickname1"),
                new User("login2", "password", "secret", "nickname2"),
                new User("login3", "password", "secret", "nickname3"),
                new User("login4", "password", "secret", "nickname4"),
                new User("login5", "password", "secret", "nickname5")
        ));
    }

    @Override
    public void start() {
        System.out.println("Auth service started");
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");
    }

    @Override
    public String authorizeUserByLoginAndPassword(String login, String password) {
        for (User user : usersList) {
            if (login.equalsIgnoreCase(user.getLogin()) && password.equals(user.getPassword())) {
                return user.getNickname();
            }
        }
        throw new WrongCredentialsExceptions("Wrong username password");
    }

    @Override
    public String changeNickname(String login, String newNickname) {
        return null;
    }

    @Override
    public User createNewUser(String login, String password, String nickname) {
        return null;
    }

    @Override
    public void deleteUser(String login, String password) {

    }

    @Override
    public void changePassword(String login, String oldPassword, String newPassword) {

    }

    @Override
    public void resetPassword(String login, String newPassword, String secretPhrase) {

    }
}
