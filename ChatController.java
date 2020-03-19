package chatclient;

public class ChatController {

    private ClientApplication clientApplication;

    public ChatController() {
        int id = Database.getId(Client.getInstance().getUsername());
        clientApplication = new ClientApplication("127.0.0.1", 1111, id);
        
    }
}
