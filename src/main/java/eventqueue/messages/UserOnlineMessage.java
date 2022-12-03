package eventqueue.messages;

public class UserOnlineMessage extends LiveMessage {

    private final long userID;

    public UserOnlineMessage(long userID) {
        super("user-online");
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "UserOnlineMessage{" +
        "userID=" + userID +
        ", type='" + type + '\'' +
        '}';
    }
}
