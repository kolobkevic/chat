package ru.kolobkevic.chat.chat_client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.kolobkevic.chat.chat_client.network.MessageProcessor;
import ru.kolobkevic.chat.chat_client.network.NetworkService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainChatController implements Initializable, MessageProcessor {
    public static final String REGEX = "%!%";

    private String nickname;
    private NetworkService networkService;

    @FXML
    public VBox loginPanel;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public VBox mainChatPanel;

    @FXML
    public TextArea mainChatArea;

    @FXML
    public ListView contactsList;

    @FXML
    public TextField inputField;

    @FXML
    public Button btnSend;

    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void someAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        var message = inputField.getText();
        if (message.isBlank()) {
            return;
        }
//            if (contactsList.getSelectionModel().isEmpty()) {
//                mainChatArea.appendText("ALL: " + message + System.lineSeparator());
//            } else {
//                mainChatArea.appendText(contactsList.getSelectionModel().getSelectedItem().toString() + ": " + message + System.lineSeparator());
//            }
        networkService.sendMessage("/broadcast" + REGEX + nickname + REGEX + message);
        inputField.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.networkService = new NetworkService(this);
    }

    @Override
    public void processMessage(String message) {
        Platform.runLater(()->parseIncomingMessage(message));
    }

    public void parseIncomingMessage(String message) {
        var splitMessage = message.split(REGEX);
        switch (splitMessage[0]) {
            case "/auth_ok":
                this.nickname = splitMessage[1];
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            case "/broadcast":
                mainChatArea.appendText(splitMessage[1] + ": " + splitMessage[2] + System.lineSeparator());
                break;
            case "/error":
                showError(splitMessage[1]);
                System.out.println("Got error " + splitMessage[1]);
                break;
            case "/list":
                var contacts = new ArrayList<String>();
                contacts.add("ALL");
                for (int i = 1; i < splitMessage.length; i++) {
                    contacts.add(splitMessage[i]);
                }
                contactsList.setItems(FXCollections.observableList(contacts));
                break;
        }
    }

    private void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR, "An error occurred:" + message, ButtonType.OK);
        alert.showAndWait();
    }

    public void sendAuth(ActionEvent actionEvent) {
        var login = loginField.getText();
        var password = passwordField.getText();
        if (login.isBlank() || password.isBlank()) {
            return;
        }
        var message = "/auth" + REGEX + login + REGEX + password;
        if (!networkService.isConnected()) {
            try {
                networkService.connect();
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
        networkService.sendMessage(message);
    }
}
