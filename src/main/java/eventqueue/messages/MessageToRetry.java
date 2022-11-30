package eventqueue.messages;

public class MessageToRetry {

    // live socket identifier:
    private final long userID;
    private final String userToken;

    // message itself:
    private final EventMessage message;

    private int attemptsLeft = 5;

    public MessageToRetry(long userID, String userToken, EventMessage message) {
        this.userID = userID;
        this.userToken = userToken;
        this.message = message;
    }

    public long userID() { return userID; }
    public String userToken() { return userToken; }
    public EventMessage message() { return message; }

    public int attemptsLeft() {
        return attemptsLeft;
    }

    public boolean attempt() {
        if (attemptsLeft == 0) return false;
        attemptsLeft--;
        return true;
    }

    @Override
    public String toString() {
        return "MessageToRetry{" +
        "userID=" + userID +
        ", userToken='" + userToken + '\'' +
        ", message=" + message +
        ", attemptsLeft=" + attemptsLeft +
        '}';
    }
}
