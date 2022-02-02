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
            e.printStackTrace();
        }
    }

    public void handle() {
        handlerThread = new Thread(() -> {
            authorize();
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                try {
                    var message = in.readUTF();
                    handleMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        handlerThread.start();
    }

    private void handleMessage(String message) {
        var splitMessage = message.split(Server.REGEX);
        switch (splitMessage[0]) {
            case "/broadcast":
                server.broadcastMessage(user, splitMessage[1]);
                break;
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
                        System.out.println("Wrong credentials" + nickname);
                    }
                    if (!response.isEmpty()) {
                        send(response);
                    } else {
                        this.user = nickname;
                        server.addAuthorizedUserToList(this);
                        send("/auth_ok" + Server.REGEX + nickname);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}