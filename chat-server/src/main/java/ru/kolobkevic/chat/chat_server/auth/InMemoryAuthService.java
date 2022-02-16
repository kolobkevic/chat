package ru.kolobkevic.chat.chat_server.auth;

import ru.kolobkevic.chat.chat_server.entity.User;
import ru.kolobkevic.chat.chat_server.error.WrongCredentialsExceptions;
import ru.kolobkevic.chat.chat_server.server.DataBaseHandler;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {
    private List<User> usersList;
    private DataBaseHandler dataBaseHandler;


    public InMemoryAuthService() {
        this.usersList = new ArrayList<>();
        dataBaseHandler = new DataBaseHandler();
        usersList.addAll(dataBaseHandler.getListFromDB());
    }

    @Override
    public void start() {
        System.out.println("Auth service started");
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");
        dataBaseHandler.disconnect();
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
        return dataBaseHandler.changeNickname(login, newNickname);
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
