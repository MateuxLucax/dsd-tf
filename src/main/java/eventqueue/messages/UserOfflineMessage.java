package eventqueue.messages;

public class UserOfflineMessage extends EventMessage {

    private final long userID;

    public UserOfflineMessage(long userID) {
        super("user-offline");
        this.userID = userID;
    }
}
