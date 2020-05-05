package chatapplication.client;


class Message {
    private int senderId;
    private String message;
    private boolean received;

    public Message(int senderId, String message, boolean received) {
        this.senderId = senderId;
        this.message = message;
        this.received = received;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isReceived() {
        return received;
    }

    public boolean isFromCurrentInterlocutor(int interlocutorId) {
        return (senderId == interlocutorId);
    }
}
