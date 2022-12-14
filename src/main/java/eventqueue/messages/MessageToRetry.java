package eventqueue.messages;

public class MessageToRetry {

    // live socket identifier:
    private final long userID;
    private final String userToken;

    // message itself:
    private final LiveMessage message;

    private int attemptsLeft = 5;

    public MessageToRetry(long userID, String userToken, LiveMessage message) {
        this.userID = userID;
        this.userToken = userToken;
        this.message = message;
    }

    public long userID() { return userID; }
    public String userToken() { return userToken; }
    public LiveMessage message() { return message; }

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
