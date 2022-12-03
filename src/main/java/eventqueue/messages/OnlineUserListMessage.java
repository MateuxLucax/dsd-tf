package eventqueue.messages;

import java.util.Collection;

public class OnlineUserListMessage extends LiveMessage {

    private final Collection<Long> userIDs;

    public OnlineUserListMessage(Collection<Long> userIDs) {
        super("online-user-list");
        this.userIDs = userIDs;
    }

    @Override
    public String toString() {
        return "OnlineUserListMessage{" +
        "userIDs=" + userIDs +
        ", type='" + type + '\'' +
        '}';
    }
}
