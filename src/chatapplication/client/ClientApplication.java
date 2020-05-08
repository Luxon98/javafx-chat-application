package chatapplication.client;

import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static chatapplication.client.Command.*;


class ClientApplication {
    private int userId;
    private boolean friendsStatusesUpdateFlag = false;
    private boolean redrawPanelFlag = false;
    private Socket socket;
    private List<Friend> friendsList;
    private Queue<Message> messagesQueue = new LinkedList<>();
    private Queue<Integer> invitationsQueue = new LinkedList<>();

    public ClientApplication(int id) {
        userId = id;
        if (!initDataOutputStream()) {
            return;
        }
        fillFriendsList();
        fillInvitationsQueue();

        listen();
        checkFriendsStatuses();
    }

    private boolean initDataOutputStream() {
        try {
            socket = new Socket(ServerConnection.SERVER_ADDRESS, ServerConnection.SERVER_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(CONNECT.getCommandNumber());
            dataOutputStream.writeInt(userId);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void fillFriendsList() {
        friendsList = new ArrayList<>();
        int[] friendsIds = DatabaseQueries.getFriendsIds(userId);
        for (int friendId : friendsIds) {
            String username = DatabaseQueries.getUsername(friendId);
            friendsList.add(new Friend(friendId, username, false));
        }
    }

    private void fillInvitationsQueue() {
        Integer[] prospectiveFriendsIds = DatabaseQueries.getPendingInvitations(userId);
        Collections.addAll(invitationsQueue, prospectiveFriendsIds);
        DatabaseQueries.removePendingInvitations(userId);
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    if (dataInputStream.available() > 0) {
                        int commandNumber = dataInputStream.readInt();
                        Command command = Command.fromInteger(commandNumber);
                        executeCommand(command, dataInputStream);
                    }
                }
                disconnect();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    private void executeCommand(Command command, DataInputStream dataInputStream) {
        if (command == Command.MESSAGE) {
            receiveMessage(dataInputStream);
        }
        else if (command == Command.FRIENDS_STATUSES) {
            updateFriendsStatus(dataInputStream);
        }
        else if (command == Command.INVITATION) {
            receiveInvitation(dataInputStream);
        }
        else if (command == Command.REDRAW_PANEL) {
            redrawPanelFlag = true;
        }
    }

    private void receiveMessage(DataInputStream dataInputStream) {
        try {
            int senderId = dataInputStream.readInt();
            String text = dataInputStream.readUTF();
            messagesQueue.add(new Message(senderId, text, true));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFriendsStatus(DataInputStream dataInputStream) {
        try {
            for (Friend friend : friendsList) {
                friend.setActiveStatus(dataInputStream.readBoolean());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        friendsStatusesUpdateFlag = true;
    }

    private void receiveInvitation(DataInputStream dataInputStream) {
        try {
            int senderId = dataInputStream.readInt();
            invitationsQueue.add(senderId);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFriendsStatuses() {
        Thread thread = new Thread(() -> {
            try {
                Instant beginning = Instant.now();
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    Instant end = Instant.now();
                    if (Duration.between(beginning, end).toMillis() > 15000) {
                        sendFriendStatusesRequest(dataOutputStream);
                        beginning = end;
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void sendFriendStatusesRequest(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(FRIENDS_STATUSES.getCommandNumber());
        dataOutputStream.writeInt(friendsList.size());
        for (Friend friend : friendsList) {
            dataOutputStream.writeInt(friend.getId());
        }
    }

    public void sendMessage(int receiverId, String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(MESSAGE.getCommandNumber());
            dataOutputStream.writeInt(receiverId);
            dataOutputStream.writeUTF(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInvitation(int receiverId) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(INVITATION.getCommandNumber());
            dataOutputStream.writeInt(receiverId);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRedrawPanelCommand(int receiverId) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(REDRAW_PANEL.getCommandNumber());
            dataOutputStream.writeInt(receiverId);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            dataOutputStream.writeInt(DISCONNECT.getCommandNumber());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateFriendsList() {
        fillFriendsList();
    }

    public void unsetUpdateFlag() {
        friendsStatusesUpdateFlag = false;
    }

    public boolean areFriendsStatusesUpdated() {
        return friendsStatusesUpdateFlag;
    }

    public void unsetRedrawPanelFlag() {
        redrawPanelFlag = false;
    }

    public boolean shouldPanelBeRedrawn() {
        return redrawPanelFlag;
    }

    public int getUserId() {
        return userId;
    }

    public int getListIndex(int id) {
        for (int i = 0; i < friendsList.size(); ++i) {
            if (friendsList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public boolean isAlreadyFriend(String name) {
        for (Friend friend : friendsList) {
            if (friend.getUsername().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFriendOnline(int friendId) {
        for (Friend friend : friendsList) {
            if (friend.getId() == friendId) {
                return friend.isActive();
            }
        }
        return false;
    }

    public List<Friend> getFriendsList() {
        return friendsList;
    }

    public boolean containsMessages() {
        return (!messagesQueue.isEmpty());
    }

    public Message getMessage() {
        return messagesQueue.remove();
    }

    public boolean containsInvitations() {
        return (!invitationsQueue.isEmpty());
    }

    public int getInvitingUserId() {
        return invitationsQueue.remove();
    }

    public Boolean[] getFriendsStatuses() {
        return friendsList.stream()
                .map(Friend::isActive)
                .toArray(Boolean[]::new);
    }
}