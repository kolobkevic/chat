package ru.kolobkevic.chat.chat_server.entity;

public class User {
    private String login;
    private String password;
    private String secret;
    private String nickname;


    public User(String login, String password, String secret, String nickname) {
        this.login = login;
        this.password = password;
        this.secret = secret;
        this.nickname = nickname;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
