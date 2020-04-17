package chatclient;

public class Message {
    private int senderId;
    private String message;

    public Message(int senderId, String message) {
        this.senderId = senderId;
        this.message = message;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromCurrentInterlocutor(int interlocutorId) {
        return (senderId == interlocutorId);
    }
}
