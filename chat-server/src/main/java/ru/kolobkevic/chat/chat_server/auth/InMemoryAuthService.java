package ru.kolobkevic.chat.chat_server.auth;

import ru.kolobkevic.chat.chat_server.entity.User;
import ru.kolobkevic.chat.chat_server.error.WrongCredentialsExceptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {
    private List<User> usersList;
    private Connection connection;
    private Statement statement;
    private PreparedStatement ps_ChangeNickname;

    private final String DB_CONNECTION_STRING = "jdbc:sqlite:chat-server/db/UserListDB.db";
    private final String DB_CHANGE_NICKNAME_STRING = "UPDATE userList SET nickname = ? WHERE login = ?;";


    public InMemoryAuthService() {
        this.usersList = new ArrayList<>();
        try {
            connect();
            readFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        finally {
//            disconnect();
//        }
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
        try {
            ps_ChangeNickname.setString(1, newNickname);
            ps_ChangeNickname.setString(2, login);
            ps_ChangeNickname.executeUpdate();
            return newNickname;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_CONNECTION_STRING);
        statement = connection.createStatement();
        ps_ChangeNickname = connection.prepareStatement(DB_CHANGE_NICKNAME_STRING);
    }

    private void readFromDB() throws SQLException {
        try (var resultSet = statement.executeQuery("SELECT * FROM userList;")) {
            while (resultSet.next()) {
                usersList.addAll(List.of(
                        new User(resultSet.getString("login"), resultSet.getString("password"),
                                resultSet.getString("secret"), resultSet.getString("nickname"))));
            }
        }
    }

    private void disconnect() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps_ChangeNickname != null) {
            try {
                ps_ChangeNickname.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
