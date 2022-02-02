package ru.kolobkevic.chat.chat_server.server;

import ru.kolobkevic.chat.chat_server.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static final String REGEX = "%!%";
    private static final int PORT = 15390;
    private List<UserHandler> userHandlerList;

    public AuthService getAuthService() {
        return authService;
    }

    private AuthService authService;

    public Server(AuthService authService) {
        this.userHandlerList = new ArrayList<>();
        this.authService = authService;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            while (true) {
                System.out.println("Waiting for connections");
                Socket socket = serverSocket.accept();
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
        message = "/broadcast" + REGEX + from + REGEX + message;
        for (UserHandler userHandler : userHandlerList) {
            userHandler.send(message);
        }
    }

    public void privateMessage(String from, String message) {

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
        var sb = new StringBuilder("/list:");
        sb.append(REGEX);
        for (UserHandler userHandler : userHandlerList) {
            sb.append(userHandler.getUserNickname());
            sb.append(REGEX);
        }
        var message = sb.toString();

        for (UserHandler userHandler : userHandlerList) {
            broadcastMessage(userHandler.getUserNickname(), message);
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
}