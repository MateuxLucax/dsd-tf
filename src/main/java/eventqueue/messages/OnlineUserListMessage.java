package eventqueue.messages;

import java.util.Collection;

public class OnlineUserListMessage extends EventMessage {
    private final Collection<Long> userIDs;
    public OnlineUserListMessage(Collection<Long> userIDs) {
        super("online-user-list");
        this.userIDs = userIDs;
    }
    public Collection<Long> userIDs() { return userIDs; }
}
