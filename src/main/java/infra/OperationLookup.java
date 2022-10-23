package infra;

import friends.FinishFriendRequest;
import friends.GetFriendRequests;
import friends.SendFriendRequest;
import meta.GetAllErrorCodes;
import meta.GetOperationIndex;
import user.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OperationLookup {

    public interface RequestHandlerConstructor {
        RequestHandler constructor(Request req, SharedContext ctx);
    }

    private static final Map<String, RequestHandlerConstructor> map;

    static {
        map = new HashMap<>();

        map.put("get-all-error-codes", GetAllErrorCodes::new);
        map.put("get-index", GetOperationIndex::new);

        map.put("create-user", CreateUser::new);
        map.put("create-session", CreateSession::new);
        map.put("whoami", Whoami::new);
        map.put("search-users", SearchUsers::new);

        map.put("get-friend-requests", GetFriendRequests::new);
        map.put("send-friend-request", SendFriendRequest::new);
        map.put("finish-friend-request", FinishFriendRequest::new);
    }

    public static Collection<String> names() {
        return map.keySet();
    }

    public static Optional<RequestHandlerConstructor> get(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}