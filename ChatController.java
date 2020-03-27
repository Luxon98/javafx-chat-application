package chatclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.image.Image ;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label interlocutorLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Pane messagesPane;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private Pane friendsListPane;


    private Image[] images = new Image[2];
    private ClientApplication clientApplication;
    //private int currentInterlocutorId = 1;
    private int currentMessagesCounter;
    private AtomicBoolean inUse = new AtomicBoolean();

    @FXML
    public void initialize() {
        int id = Database.getId(Client.getInstance().getUsername());
        clientApplication = new ClientApplication("127.0.0.1", 4567, id);
        currentMessagesCounter = 0;
        loadImages();
        drawUserPanel();
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
        currentMessagesCounter = 0;
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

    private void loadImages() {
        images[0] = new Image("file:images/haze.png");
        images[1] = new Image("file:images/default_avatar.png");
    }

    private void drawUserPanel() {
        ImageView imageView = new ImageView(images[1]);
        imageView.setLayoutX(18);
        imageView.setLayoutY(12);
        friendsListPane.getChildren().add(imageView);
        usernameLabel.setText(Client.getInstance().getUsername());
    }

    private void drawFriendsPanel() {
        List<User> friendsList = clientApplication.getFriendsList();
        int positionY = 65;
        for (User user : friendsList) {
            Pane pane = new Pane();
            pane.setPrefHeight(55);
            pane.setPrefWidth(200);
            pane.setLayoutY(positionY);
            pane.setStyle("-fx-border-color: aliceblue; -fx-border-color: #a2a3a2; -fx-border-width: 0 0 1 0;");

            Label label = new Label();
            label.setLayoutX(30);
            label.setLayoutY(25);
            label.setTextFill(Color.WHITE);
            label.setText(user.getLogin());

            pane.getChildren().add(label);
            pane.setOnMouseClicked(t -> {
                interlocutorLabel.setText(label.getText());
                removeMessages();
                messagesPane.setPrefHeight(485);
            });
            friendsListPane.getChildren().add(pane);
            positionY += 55;
        }
    }

    private void drawSentMessageLabel(String message) {
        Label label = new Label();
        label.setStyle("-fx-background-color: #05b529; -fx-padding: 3 3 3 3;");
        label.setPrefHeight(message.length() > 30 ? 46 : 23);
        label.setFont(new Font("Arial", 13));
        label.setTextAlignment(TextAlignment.CENTER);
        label.setText(message);
        label.setLayoutY(10 + currentMessagesCounter * 35);

        Text text = new Text(label.getText());
        text.setFont(label.getFont());
        label.setLayoutX(553 - text.getBoundsInLocal().getWidth());

        messagesPane.getChildren().add(label);
        ++currentMessagesCounter;

        if (currentMessagesCounter >= 13) {
            messagesPane.setPrefHeight(messagesPane.getHeight() + 35);
            messagesScrollPane.setVvalue(1.0);
        }
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
        label.setLayoutY(10 + currentMessagesCounter * 35);

        messagesPane.getChildren().add(label);
        ++currentMessagesCounter;

        if (currentMessagesCounter >= 13) {
            messagesPane.setPrefHeight(messagesPane.getHeight() + 35);
            messagesScrollPane.setVvalue(1.0);
        }
    }
}
