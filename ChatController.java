package chatclient;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatController {

    @FXML
    private Label usernameLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Pane messagesPane;

    @FXML
    Pane friendsListPane;

    private ClientApplication clientApplication;
    //private int currentInterlocutorId = 1;
    private int currentSentMessagesCounter = 0;
    private int currentReceivedMessagesCounter = 0;
    private AtomicBoolean inUse = new AtomicBoolean();

    @FXML
    public void initialize() {
        int id = Database.getId(Client.getInstance().getUsername());
        usernameLabel.setText(Client.getInstance().getUsername());
        clientApplication = new ClientApplication("127.0.0.1", 4567, id);
        drawFriendsPanel();
        checkForMessages();
    }

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText();
        if (message != null) {
            clientApplication.sendMessage(1, message);
            messageTextArea.setText(null);

            if (message.length() > 30) {
                message = message.substring(0, 30) + "\n" + message.substring(30);
            }

            drawSentMessageLabel(message);
        }
    }

    @FXML
    private void handleTextAreaKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String message = messageTextArea.getText();
            message = message.substring(0, message.length() - 1);
            messageTextArea.setText(message);

            sendMessage();
        }
    }

    @FXML
    private void removeMessages() {
        messagesPane.getChildren().clear();
        currentSentMessagesCounter = 0;
        currentReceivedMessagesCounter = 0;
    }

    private void checkForMessages() {
        Thread thread = new Thread(() -> {
            while (!Client.getInstance().isProgramClosed()) {
                if (clientApplication.containsMessage() && inUse.compareAndSet(false, true)) {
                    String message = clientApplication.getMessage();
                    Platform.runLater(() -> {
                        drawReceivedMessageLabel(message);
                    });
                    inUse.set(false);
                }
            }
        });
        thread.start();
    }

    private void drawFriendsPanel() {
        List<User> friendsList = clientApplication.getFriendsList();
        int positionY = 80;
        for (User user : friendsList) {
            Pane pane = new Pane();
            pane.setPrefHeight(80);
            pane.setPrefWidth(200);
            pane.setLayoutY(positionY);
            pane.setStyle("-fx-border-color: aliceblue");

            Label label = new Label();
            label.setLayoutX(30);
            label.setLayoutY(25);
            label.setTextFill(Color.WHITE);
            label.setText(user.getLogin());

            pane.getChildren().add(label);
            friendsListPane.getChildren().add(pane);
            positionY += 80;
        }
    }

    private void drawSentMessageLabel(String message) {
        Label label = new Label();
        label.setStyle("-fx-background-color: #05b529; -fx-padding: 3 3 3 3;");
        label.setPrefHeight(message.length() > 30 ? 46 : 23);
        label.setFont(new Font("Arial", 13));
        label.setTextAlignment(TextAlignment.CENTER);
        label.setText(message);
        label.setLayoutY(20 + currentSentMessagesCounter * 70);

        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        label.setLayoutX(570 - text.getBoundsInLocal().getWidth());

        messagesPane.getChildren().add(label);
        ++currentSentMessagesCounter;
    }

    public void drawReceivedMessageLabel(String message) {
        Label label = new Label();
        label.setStyle("-fx-background-color: #9752ff; -fx-padding: 3 3 3 3;");
        label.setTextFill(Color.WHITE);
        label.setPrefHeight(message.length() > 30 ? 46 : 23);
        label.setFont(new Font("Arial", 13));
        label.setTextAlignment(TextAlignment.CENTER);
        label.setText(message);

        label.setLayoutX(25);
        label.setLayoutY(55 + currentReceivedMessagesCounter * 70);

        messagesPane.getChildren().add(label);
        ++currentReceivedMessagesCounter;
    }
}
