package ru.kolobkevic.chat.chat_client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainChatController implements Initializable {
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
        if (message.isBlank()){
            return;
        }
        else {
            if (contactsList.getSelectionModel().isEmpty()) {
                mainChatArea.appendText("ALL: " + message + System.lineSeparator());
            }
            else {
                mainChatArea.appendText(contactsList.getSelectionModel().getSelectedItem().toString() + ": " + message + System.lineSeparator());
            }
            inputField.clear();

        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var contacts = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            contacts.add("Contact№ " + (i + 1));
        }

        contactsList.setItems(FXCollections.observableList(contacts));
    }

}
