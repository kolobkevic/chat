package ru.kolobkevic.chat.chat_server.server;

import ru.kolobkevic.chat.chat_server.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler {
    private Connection connection;
    private Statement statement;
    private PreparedStatement ps_ChangeNickname;

    private List listFromDB;

    private final String DB_CONNECTION_STRING = "jdbc:sqlite:chat-server/db/UserListDB.db";
    private final String DB_CHANGE_NICKNAME_STRING = "UPDATE userList SET nickname = ? WHERE login = ?;";

    public DataBaseHandler() {
        listFromDB = new ArrayList<>();
        try {
            connect();
            readFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_CONNECTION_STRING);
        statement = connection.createStatement();
        ps_ChangeNickname = connection.prepareStatement(DB_CHANGE_NICKNAME_STRING);
    }

    private void readFromDB() throws SQLException {
        try (var resultSet = statement.executeQuery("SELECT * FROM userList;")) {
            while (resultSet.next()) {
                listFromDB.addAll(List.of(
                        new User(resultSet.getString("login"), resultSet.getString("password"),
                                resultSet.getString("secret"), resultSet.getString("nickname"))));
            }
        }
    }

    public void disconnect() {
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

    public List getListFromDB() {
        return listFromDB;
    }
}
