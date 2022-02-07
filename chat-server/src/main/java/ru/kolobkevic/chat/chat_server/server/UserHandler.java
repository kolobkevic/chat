package ru.kolobkevic.chat.chat_server.server;

import ru.kolobkevic.chat.chat_server.error.WrongCredentialsExceptions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UserHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread handlerThread;
    private String user;

    public UserHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Connection is broken with user " + user);
        }
    }

    public void handle() {
        handlerThread = new Thread(() -> {
            authorize();
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    var message = in.readUTF();
                    handleMessage(message);
                } catch (IOException e) {
                    System.out.println("Connection is broken with user " + user);
                    server.removeAuthorizedUserToList(this);
                }
            }
        });
        handlerThread.start();
    }

    private void handleMessage(String message) {
        var splitMessage = message.split(Server.REGEX);
        try {
            switch (splitMessage[0]) {
                case "/w":
                    server.privateMessage(this.user, splitMessage[1], splitMessage[2], this);
                    break;
                case "/broadcast":
                    server.broadcastMessage(user, splitMessage[1]);
                    break;
                case "/change_nick":
                    String nick = server.getAuthService().changeNickname(this.user, splitMessage[1]);
                    server.removeAuthorizedUserToList(this);
                    this.user = nick;
                    server.addAuthorizedUserToList(this);
                    send("/change_nick_ok");
                    break;
                case "/change_pass":
                    server.getAuthService().changePassword(this.user, splitMessage[1], splitMessage[2]);
                    send("/change_pass_ok");
                    break;
                case "/remove":
                    server.getAuthService().deleteUser(splitMessage[1], splitMessage[2]);
                    this.socket.close();
                    break;
                case "/register":
                    server.getAuthService().createNewUser(splitMessage[1], splitMessage[2], splitMessage[3]);
                    send("register_ok:");
                    break;
            }
        } catch (Exception e) {
            send("/error" + Server.REGEX + e.getMessage());
        }
    }

    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserNickname() {
        return this.user;
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }

    private void authorize() {
        System.out.println("Authorizing");
        while (true) {
            try {
                var message = in.readUTF();
                if (message.startsWith("/auth")) {
                    var parsedAuthMessage = message.split(Server.REGEX);
                    var response = "";
                    String nickname = null;
                    try {
                        nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsExceptions e) {
                        response = "/error" + Server.REGEX + e.getMessage();
                        System.out.println("Wrong credentials, nick " + parsedAuthMessage[1]);
                    }

                    if (server.isNicknameBusy(nickname)) {
                        response = "/error" + Server.REGEX + "This user is already connected";
                        System.out.println("Nickname is busy" + nickname);
                    }
                    if (!response.isEmpty()) {
                        send(response);
                    } else {
                        this.user = nickname;
                        send("/auth_ok" + Server.REGEX + nickname);
                        server.addAuthorizedUserToList(this);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}