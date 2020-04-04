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
import javafx.scene.text.Text;
import javafx.scene.image.Image;
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

    private Pane[] friendsPanes;
    private Image[] images;
    private ClientApplication clientApplication;
    private AtomicBoolean inUse;
    private int currentInterlocutorId;
    private int currentMessagesCounter;


    @FXML
    public void initialize() {
        int id = Database.getId(Client.getInstance().getUsername());
        clientApplication = new ClientApplication("127.0.0.1", 4567, id);
        inUse =  new AtomicBoolean();
        currentMessagesCounter = 0;
        currentInterlocutorId = id;
        initImages();
        drawUserPanel();
        drawFriendsPanel();
        checkForMessages();
    }

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText();
        if (message != null) {
            clientApplication.sendMessage(currentInterlocutorId, message);
            messageTextArea.setText(null);

            if (message.length() > 30) {
                message = message.substring(0, 30) + "\n" + message.substring(30);
            }

            drawMessageLabel(message, false);
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

    @FXML
    private void test() {
        clientApplication.sendTest();
    }

    private void checkForMessages() {
        Thread thread = new Thread(() -> {
            while (!Client.getInstance().isProgramClosed()) {
                if (inUse.compareAndSet(false, true)) {
                    if (clientApplication.containsMessage()) {
                        Message message = clientApplication.getMessage();
                        if (message.getSenderId() == currentInterlocutorId) {
                            Platform.runLater(() -> {
                                drawMessageLabel(message.getMessage(), true);
                            });
                        }
                        else {
                            //wyslij do bazy

                            int index = clientApplication.getListIndex(message.getSenderId());
                            if (index != -1) {
                                ImageView iw = (ImageView) friendsPanes[0].lookup("#messageImg");
                                iw.setVisible(true);
                            }
                        }
                    }
                    Boolean[] friendsStatuses = clientApplication.getFriendsStatuses();
                    for (int i = 0; i < friendsStatuses.length; ++i) {
                        ImageView iw = (ImageView) friendsPanes[i].lookup("#statusImg");
                        if (!friendsStatuses[i]) {
                            iw.setImage(images[2]);
                        }
                        else {
                            iw.setImage(images[3]);
                        }
                    }
                    inUse.set(false);
                }
            }
        });
        thread.start();
    }

    private void initImages() {
        images = new Image[4];
        images[0] = new Image("file:images/haze.png");
        images[1] = new Image("file:images/default_avatar.png");
        images[2] = new Image("file:images/green_circle.png");
        images[3] = new Image("file:images/red_circle.png");
    }

    private void drawUserPanel() {
        ImageView img = new ImageView(images[1]);
        img.setLayoutX(18);
        img.setLayoutY(12);
        friendsListPane.getChildren().add(img);
        usernameLabel.setText(Client.getInstance().getUsername());
    }

    private void drawFriendsPanel() {
        List<Friend> friendsList = clientApplication.getFriendsList();
        friendsPanes = new Pane[friendsList.size()];

        int index = 0, positionY = 65;
        for (Friend friend : friendsList) {
            friendsPanes[index] = new Pane();
            friendsPanes[index].setPrefSize(200, 55);
            friendsPanes[index].setLayoutY(positionY);
            friendsPanes[index].setStyle("-fx-border-color: aliceblue; -fx-border-color: #a2a3a2; -fx-border-width: 0 0 1 0;");

            ImageView img = new ImageView(images[1]);
            img.setLayoutX(18);
            img.setLayoutY(12);

            ImageView messageImg = new ImageView(images[0]);
            messageImg.setLayoutX(150);
            messageImg.setLayoutY(16);
            messageImg.setId("messageImg");
            messageImg.setVisible(false);


            ImageView statusImg = new ImageView(images[3]);
            statusImg.setLayoutX(38);
            statusImg.setLayoutY(31);
            statusImg.setId("statusImg");


            Label label = new Label();
            label.setLayoutX(53);
            label.setLayoutY(16);
            label.setTextFill(Color.WHITE);
            label.setText(friend.getLogin());

            friendsPanes[index].getChildren().add(messageImg);
            friendsPanes[index].getChildren().add(img);
            friendsPanes[index].getChildren().add(statusImg);
            friendsPanes[index].getChildren().add(label);
            friendsPanes[index].setOnMouseClicked(t -> {
                interlocutorLabel.setText(label.getText());
                currentInterlocutorId = friend.getId();
                removeMessages();
                messagesPane.setPrefHeight(485);
                messageImg.setVisible(false);
            });
            friendsListPane.getChildren().add(friendsPanes[index]);
            positionY += 55;
            ++index;
        }
    }

    private void drawMessageLabel(String message, boolean isReceived) {
        Label label = new Label();
        label.setPrefHeight(message.length() > 30 ? 46 : 23);
        label.setText(message);

        if (isReceived) {
            label.setStyle("-fx-background-color: #eb34d2; -fx-padding: 4 4 4 4; -fx-background-radius: 3; " +
                    "-fx-font-family: Arial; -fx-font-size: 14; -fx-text-fill: #ffffff;-fx-text-alignment: center;");
            label.setLayoutX(28);
        }
        else {
            label.setStyle("-fx-background-color: #05b529; -fx-padding: 4 4 4 4; -fx-background-radius: 3; " +
                    "-fx-font-family: Arial; -fx-font-size: 14; -fx-text-fill: #ffffff;-fx-text-alignment: center");
            Text text = new Text(label.getText());
            text.setFont(label.getFont());
            label.setLayoutX(546 - text.getBoundsInLocal().getWidth());
        }
        label.setLayoutY(10 + currentMessagesCounter * 35);

        messagesPane.getChildren().add(label);
        ++currentMessagesCounter;

        if (currentMessagesCounter >= 13) {
            messagesPane.setPrefHeight(messagesPane.getHeight() + 40);
            messagesScrollPane.setVvalue(1.0);
        }
    }
}
