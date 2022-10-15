package infra;

import friends.GetFriendRequests;
import friends.SendFriendRequest;
import user.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OperationLookup {

    @FunctionalInterface
    public interface RequestHandlerConstructor {
        RequestHandler constructor(Request req, ResponseWriter res, SharedContext ctx);
    }

    private static final Map<String, RequestHandlerConstructor> map;

    static {
        map = new HashMap<>();
        map.put("create-user", CreateUser::new);
        map.put("create-session", CreateSession::new);
        map.put("whoami", Whoami::new);
        map.put("search-users", SearchUsers::new);
        map.put("get-friend-requests", GetFriendRequests::new);
        map.put("send-friend-request", SendFriendRequest::new);
    }

    public static Optional<RequestHandlerConstructor> get(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}