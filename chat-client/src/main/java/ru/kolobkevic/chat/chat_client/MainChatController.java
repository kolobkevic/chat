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
    public VBox changeNickPanel;
    public TextField newNickField;
    public VBox changePasswordPanel;
    public PasswordField oldPassField;
    public PasswordField newPasswordField;

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
        var user = contactsList.getSelectionModel().getSelectedItem();
        if (!user.equals("ALL")) {
            networkService.sendMessage("/w" + REGEX + user + REGEX + message);
        } else {
            networkService.sendMessage("/broadcast" + REGEX + message);
        }
        inputField.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.networkService = new NetworkService(this);
    }

    @Override
    public void processMessage(String message) {
        Platform.runLater(() -> parseIncomingMessage(message));
    }

    public void parseIncomingMessage(String message) {
        var splitMessage = message.split(REGEX);
        switch (splitMessage[0]) {
            case "/auth_ok":
                this.nickname = splitMessage[1];
                loginPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            case "/error":
                showError(splitMessage[1]);
                System.out.println("Got error " + splitMessage[1]);
                break;
            case "/list":
                var contacts = new ArrayList<String>();
                contacts.add("ALL");
                for (int i = 1; i < splitMessage.length; i++) {
                    if (!splitMessage[i].equals(nickname)) {
                        contacts.add(splitMessage[i]);
                    }
                }
                contactsList.setItems(FXCollections.observableList(contacts));
                contactsList.getSelectionModel().selectFirst();
                break;
            case "/change_pass_ok":
                changePasswordPanel.setVisible(false);
                mainChatPanel.setVisible(true);
                break;
            default:
                mainChatArea.appendText(splitMessage[0] + System.lineSeparator());
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

    public void sendChangeNick(ActionEvent actionEvent) {
        if (newNickField.getText().isBlank()) return;
        networkService.sendMessage("/change_nick" + REGEX + newNickField.getText());
    }

    public void sendChangePass(ActionEvent actionEvent) {
        if (newPasswordField.getText().isBlank() || oldPassField.getText().isBlank()) return;
        networkService.sendMessage("/change_pass" + REGEX + oldPassField.getText() + REGEX + newPasswordField.getText());
    }

    public void sendEternalLogout(ActionEvent actionEvent) {
        networkService.sendMessage("/remove");
    }

    public void returnToChat(ActionEvent actionEvent) {
        changeNickPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        mainChatPanel.setVisible(true);
    }

    public void showChangeNick(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changeNickPanel.setVisible(true);
    }

    public void showChangePass(ActionEvent actionEvent) {
        mainChatPanel.setVisible(false);
        changePasswordPanel.setVisible(true);
    }
}
