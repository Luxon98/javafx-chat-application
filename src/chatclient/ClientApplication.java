package chatclient;

import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static chatclient.Command.*;
import static chatclient.ServerConnection.*;
import static chatclient.DatabaseQueries.*;


class ClientApplication {
    private int userId;
    private boolean friendsStatusesUpdateFlag = false;
    private boolean invitationAcceptedFlag = false;
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
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(CONNECT);
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
        int[] friendsIds = getFriendsIds(userId);
        for (int friendId : friendsIds) {
            String username = getUsername(friendId);
            friendsList.add(new Friend(friendId, username, false));
        }
    }

    private void fillInvitationsQueue() {
        Integer[] prospectiveFriendsIds = getPendingInvitations(userId);
        Collections.addAll(invitationsQueue, prospectiveFriendsIds);
        removePendingInvitations(userId);
    }

    private void listen() {
        Thread listenThread = new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                while (!Client.getInstance().isProgramClosed()) {
                    if (dataInputStream.available() > 0) {
                        int command = dataInputStream.readInt();
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

    private void executeCommand(int command, DataInputStream dataInputStream) {
        if (command == MESSAGE) {
            receiveMessage(dataInputStream);
        }
        else if (command == FRIENDS_STATUSES) {
            updateFriendsStatus(dataInputStream);
        }
        else if (command == INVITATION) {
            receiveInvitation(dataInputStream);
        }
        else if (command == INVITATION_ACCEPTED) {
            invitationAcceptedFlag = true;
        }
    }

    private void receiveMessage(DataInputStream dataInputStream) {
        try {
            int senderId = dataInputStream.readInt();
            String text = dataInputStream.readUTF();
            messagesQueue.add(new Message(senderId, text));
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
        dataOutputStream.writeInt(FRIENDS_STATUSES);
        dataOutputStream.writeInt(friendsList.size());
        for (Friend friend : friendsList) {
            dataOutputStream.writeInt(friend.getId());
        }
    }

    public void sendMessage(int id, String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(MESSAGE);
            dataOutputStream.writeInt(id);
            dataOutputStream.writeUTF(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInvitation(int id) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(INVITATION);
            dataOutputStream.writeInt(id);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInvitationAcceptedCommand(int id) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(INVITATION_ACCEPTED);
            dataOutputStream.writeInt(id);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            dataOutputStream.writeInt(DISCONNECT);
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

    public void unsetInvitationAcceptedFlag() {
        invitationAcceptedFlag = false;
    }

    public boolean isInvitationAccepted() {
        return invitationAcceptedFlag;
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