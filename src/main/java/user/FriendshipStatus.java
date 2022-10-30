package user;

import java.util.Optional;

public enum FriendshipStatus {
    // User is not a friend AND there's no friend request involving him
    NO_FRIEND_REQUEST,

    // User is not a friend but has sent you a friend request
    SENT_FRIEND_REQUEST,

    // User is not a friend but you have sent him a friend request
    RECEIVED_FRIEND_REQUEST,

    // User is a friend :)
    IS_FRIEND;

    public static Optional<FriendshipStatus> from(boolean isFriend, boolean sent, boolean received) {
        var sum = (isFriend ? 1 : 0) + (sent ? 1 : 0) + (received ? 1 : 0);
        if (sum > 1) return Optional.empty(); // Invalid combination

        if (isFriend) return Optional.of(IS_FRIEND);
        if (sent) return Optional.of(SENT_FRIEND_REQUEST);
        if (received) return Optional.of(RECEIVED_FRIEND_REQUEST);
        return Optional.of(NO_FRIEND_REQUEST);
    }
}
