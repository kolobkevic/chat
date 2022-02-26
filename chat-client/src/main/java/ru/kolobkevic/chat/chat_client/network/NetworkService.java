package ru.kolobkevic.chat.chat_client.network;

import ru.kolobkevic.chat.props.PropertyReader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkService {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MessageProcessor messageProcessor;

    public NetworkService(MessageProcessor messageProcessor) {
        host = PropertyReader.getInstance().getHost();
        port = PropertyReader.getInstance().getPort();
        this.messageProcessor = messageProcessor;
    }

    public void readMessages() {
        var thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    var message = in.readUTF();
                    messageProcessor.processMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Disconnected");
            }
        });
        thread.start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        readMessages();
    }

    public void writeToFile(String username, String outputString) {
        String filename = username + ".txt";
        try (DataOutputStream outFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename, true)))) {
            outFile.writeUTF(outputString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> readFromFile(String username) {
        String filename = username + ".txt";
        ArrayList<String> readMessagesList = new ArrayList<>();
        try (DataInputStream inputFile = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
            while (inputFile.available() > 0) {
                readMessagesList.add(inputFile.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readMessagesList.add("");
        }
        return readMessagesList;
    }
}