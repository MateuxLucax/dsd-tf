package events.messages;

public class UserLoggedInMessage extends EventMessage {
    private final long userID;
    public UserLoggedInMessage(long userID) {
        super("user-logged-in");
        this.userID = userID;
    }
    public long userID() { return userID; }
}
