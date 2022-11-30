package eventqueue.messages;

public class UserOnlineMessage extends EventMessage {

    private final long userID;

    public UserOnlineMessage(long userID) {
        super("user-online");
        this.userID = userID;
    }
}
