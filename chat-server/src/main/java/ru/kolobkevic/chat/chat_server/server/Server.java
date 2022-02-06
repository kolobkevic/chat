package ru.kolobkevic.chat.chat_server.server;

import ru.kolobkevic.chat.props.PropertyReader;
import ru.kolobkevic.chat.chat_server.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static final String REGEX = "%!%";
    private final int port;
    private final List<UserHandler> userHandlerList;
    private final AuthService authService;

    public Server(AuthService authService) {
        port = PropertyReader.getInstance().getPort();
        this.userHandlerList = new ArrayList<>();
        this.authService = authService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started");
            while (true) {
                System.out.println("Waiting for connections");
                var socket = serverSocket.accept();
                System.out.println("Client connected");
                UserHandler userHandler = new UserHandler(socket, this);
                userHandler.handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            shutdown();
        }
    }

    private void shutdown() {

    }

    public void broadcastMessage(String from, String message) {
        message = String.format("[%s]: %s", from, message);
        for (UserHandler userHandler : userHandlerList) {
            userHandler.send(message);
        }
    }

    public void privateMessage(String sender, String recipient, String message, UserHandler senderHandler) {
        var handler = getHandlerByUser(recipient);
        if (handler == null) {
            senderHandler.send(String.format("/error%s recipient not found: %s", REGEX, recipient));
            return;
        }
        message = String.format("[PRIVATE] [%s] -> [%s]: %s", sender, recipient, message);
        handler.send(message);
        senderHandler.send(message);
    }

    public synchronized void addAuthorizedUserToList(UserHandler userHandler) {
        userHandlerList.add(userHandler);
        sendOnlineUsers();
    }

    public synchronized void removeAuthorizedUserToList(UserHandler userHandler) {
        userHandlerList.remove(userHandler);
        sendOnlineUsers();
    }

    public void sendOnlineUsers() {
        var sb = new StringBuilder("/list");
        sb.append(REGEX);
        for (UserHandler userHandler : userHandlerList) {
            sb.append(userHandler.getUserNickname());
            sb.append(REGEX);
        }
        var message = sb.toString();

        for (UserHandler userHandler : userHandlerList) {
            userHandler.send(message);
        }
    }

    public synchronized boolean isNicknameBusy(String nickname) {
        for (UserHandler userHandler : userHandlerList) {
            if (userHandler.getUserNickname().equalsIgnoreCase(nickname)) {
                return true;
            }
        }
        return false;
    }

    private UserHandler getHandlerByUser(String username) {
        for (UserHandler clientHandler : userHandlerList) {
            if (clientHandler.getUserNickname().equals(username)) {
                return clientHandler;
            }
        }
        return null;
    }
}