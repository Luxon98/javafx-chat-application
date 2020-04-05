package chatclient;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
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

import static chatclient.ChatUtility.*;


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

    @FXML
    private Button sendMessageButton;

    private Pane[] friendsPanes;
    private Image[] images;
    private ClientApplication clientApplication;
    private AtomicBoolean inUse;
    private int currentInterlocutorId;
    private int currentMessagesCounter;


    @FXML
    public void initialize() {
        int id = Database.getId(Client.getInstance().getUsername());
        clientApplication = new ClientApplication("127.0.0.1", 4597, id);
        inUse = new AtomicBoolean();
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
        if (message != null && !message.isEmpty()) {
            clientApplication.sendMessage(currentInterlocutorId, message);
            messageTextArea.setText(null);

            if (isTooLong(message)) {
                message = splitWithNewLine(message);
            }

            drawMessageLabel(message, false);
        }
    }

    @FXML
    private void handleTextAreaKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String message = messageTextArea.getText();
            message = cutNewLineCharacter(message);
            messageTextArea.setText(message);

            sendMessage();
        }
    }

    @FXML
    private void changeSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #0ac45e;");
    }

    @FXML
    private void restoreSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #3548db;");
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
                        if (message.isFromCurrentInterlocutor(currentInterlocutorId)) {
                            Platform.runLater(() -> {
                                drawMessageLabel(message.getMessage(), true);
                            });
                        }
                        else {
                            //wyslij do bazy

                            int index = clientApplication.getListIndex(message.getSenderId());
                            if (isProperIndex(index)) {
                                showNewMessageImage(index);
                            }
                        }
                    }
                    showFriendsStatuses();
                    inUse.set(false);
                }
            }
        });
        thread.start();
    }

    private void showNewMessageImage(int index) {
        ImageView iw = (ImageView) friendsPanes[index].lookup("#messageImg");
        iw.setVisible(true);
    }

    private void showFriendsStatuses() {
        Boolean[] friendsStatuses = clientApplication.getFriendsStatuses();
        for (int i = 0; i < friendsStatuses.length; ++i) {
            ImageView statusIw = (ImageView) friendsPanes[i].lookup("#statusImg");
            statusIw.setImage(friendsStatuses[i] ? images[2] : images[3]);
        }
    }

    private void initImages() {
        images = new Image[4];
        images[0] = new Image("file:images/haze.png");
        images[1] = new Image("file:images/default_avatar.png");
        images[2] = new Image("file:images/green_circle.png");
        images[3] = new Image("file:images/red_circle.png");
    }

    private void drawUserPanel() {
        ImageView userAvatar = getUserAvatar();
        friendsListPane.getChildren().add(userAvatar);
        usernameLabel.setText(Client.getInstance().getUsername());
    }

    private ImageView getUserAvatar() {
        ImageView avatar = new ImageView(images[1]);
        avatar.setLayoutX(18);
        avatar.setLayoutY(12);
        return avatar;
    }

    private void drawFriendsPanel() {
        List<Friend> friendsList = clientApplication.getFriendsList();
        friendsPanes = new Pane[friendsList.size()];

        int index = 0, positionY = 65;
        for (Friend friend : friendsList) {
            friendsPanes[index] = new Pane();
            setFriendPaneStyle(friendsPanes[index], positionY);

            ImageView avatarImg = getFriendAvatar();
            ImageView messageImg = getMessageImage();
            ImageView statusImg = getStatusImage();

            Label friendNameLabel = getFriendNameLabel(friend);

            addFriendPaneComponents(friendsPanes[index], new ImageView[]{messageImg, avatarImg, statusImg}, friendNameLabel);
            setMouseClickEvent(friendsPanes[index], friendNameLabel, messageImg, friend.getId());

            friendsListPane.getChildren().add(friendsPanes[index]);
            positionY += 55;
            ++index;
        }
    }

    private void setFriendPaneStyle(Pane friendPane, int y) {
        friendPane.setPrefSize(200, 55);
        friendPane.setLayoutY(y);
        friendPane.setStyle("-fx-border-color: aliceblue; -fx-border-color: #a2a3a2; -fx-border-width: 0 0 1 0;");
    }

    private ImageView getFriendAvatar() {
        ImageView avatar = new ImageView(images[1]);
        avatar.setLayoutX(18);
        avatar.setLayoutY(12);
        return avatar;
    }

    private ImageView getMessageImage() {
        ImageView image = new ImageView(images[0]);
        image.setLayoutX(150);
        image.setLayoutY(16);
        image.setId("messageImg");
        image.setVisible(false);
        return image;
    }

    private ImageView getStatusImage() {
        ImageView image = new ImageView(images[3]);
        image.setLayoutX(38);
        image.setLayoutY(31);
        image.setId("statusImg");
        return image;
    }

    private Label getFriendNameLabel(Friend friend) {
        Label label = new Label();
        label.setLayoutX(53);
        label.setLayoutY(16);
        label.setTextFill(Color.WHITE);
        label.setText(friend.getLogin());
        return label;
    }

    private void addFriendPaneComponents(Pane pane, ImageView[] images, Label friendNameLabel) {
        pane.getChildren().add(images[0]);
        pane.getChildren().add(images[1]);
        pane.getChildren().add(images[2]);
        pane.getChildren().add(friendNameLabel);
    }

    private void setMouseClickEvent(Pane friendPane, Label friendNameLabel, ImageView messageImg, int id) {
        friendPane.setOnMouseClicked(t -> {
            if (currentInterlocutorId != id) {
                interlocutorLabel.setText(friendNameLabel.getText());
                currentInterlocutorId = id;
                removeMessages();
                messagesPane.setPrefHeight(485);
                messageImg.setVisible(false);
            }
        });
    }

    private void removeMessages() {
        messagesPane.getChildren().clear();
        currentMessagesCounter = 0;
    }

    private void drawMessageLabel(String message, boolean isReceived) {
        Label label = new Label();
        setLabelStyle(label, message, isReceived);

        messagesPane.getChildren().add(label);
        ++currentMessagesCounter;

        if (isTooManyMessages(currentMessagesCounter)) {
            adjustMessagesPane();
        }
    }

    private void setLabelStyle(Label label, String message, boolean isReceived) {
        label.setPrefHeight(isTooLong(message) ? 46 : 23);
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
    }

    private void adjustMessagesPane() {
        messagesPane.setPrefHeight(messagesPane.getHeight() + 40);
        messagesScrollPane.setVvalue(1.0);
    }
}
