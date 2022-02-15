package ru.kolobkevic.chat.chat_server.server;

import ru.kolobkevic.chat.chat_server.error.WrongCredentialsExceptions;
import ru.kolobkevic.chat.props.PropertyReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class UserHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread handlerThread;
    private String user_nickname;
    private String user_login;
    private final long authTimeout;

    public UserHandler(Socket socket, Server server) {
        authTimeout = PropertyReader.getInstance().getAuthTimeout();
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Connection is broken with user " + user_nickname);
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
                    System.out.println("Connection is broken with user " + user_nickname);
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
                    server.privateMessage(this.user_nickname, splitMessage[1], splitMessage[2], this);
                    break;
                case "/broadcast":
                    server.broadcastMessage(user_nickname, splitMessage[1]);
                    break;
                case "/change_nick":
                    String nick = server.getAuthService().changeNickname(this.user_login, splitMessage[1]);
                    server.removeAuthorizedUserToList(this);
                    this.user_nickname = nick;
                    server.addAuthorizedUserToList(this);
                    send("/change_nick_ok");
                    break;
                case "/change_pass":
                    server.getAuthService().changePassword(this.user_nickname, splitMessage[1], splitMessage[2]);
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
        return this.user_nickname;
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }

    private void authorize() {
        System.out.println("Authorizing");
        var timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (user_nickname == null) {
                        send("/time_out" + Server.REGEX + "Authentication timeout!");
                        Thread.sleep(50);
                        close();
                        System.out.println("Connection with client closed");
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, authTimeout);
        try {
            while (true) {
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
                        this.user_nickname = nickname;
                        this.user_login=parsedAuthMessage[1];
                        send("/auth_ok" + Server.REGEX + nickname);
                        server.addAuthorizedUserToList(this);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        this.socket.close();
        this.in.close();
        this.out.close();
    }
}