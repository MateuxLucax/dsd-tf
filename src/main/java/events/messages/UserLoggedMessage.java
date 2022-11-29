package events.messages;

public class UserLoggedMessage extends EventMessage {
    private final long userID;
    public UserLoggedMessage(long userID) {
        super("user-logged");
        this.userID = userID;
    }
    public long userID() { return userID; }
}
