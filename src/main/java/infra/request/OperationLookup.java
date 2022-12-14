package infra.request;

import eventqueue.GetOnlineUsers;
import eventqueue.GoOffline;
import eventqueue.GoOnline;
import files.GetFile;
import files.PutFile;
import friends.*;
import infra.SharedContext;
import messages.GetMessages;
import messages.SendMessage;
import meta.GetAllErrorCodes;
import meta.GetOperationIndex;
import user.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OperationLookup {

    public interface RequestHandlerConstructor {
        RequestHandler construct(Request req, SharedContext ctx);
    }

    private static final Map<String, RequestHandlerConstructor> map;

    static {
        map = new HashMap<>();

        map.put("get-all-error-codes", GetAllErrorCodes::new);
        map.put("get-index", GetOperationIndex::new);

        map.put("create-user", CreateUser::new);
        map.put("edit-user", EditUser::new);
        map.put("create-session", CreateSession::new);
        map.put("whoami", Whoami::new);
        map.put("search-users", SearchUsers::new);
        map.put("end-session", EndSession::new);

        map.put("get-friend-requests", GetFriendRequests::new);
        map.put("send-friend-request", SendFriendRequest::new);
        map.put("finish-friend-request", FinishFriendRequest::new);
        map.put("get-friends", GetFriends::new);
        map.put("remove-friend", RemoveFriend::new);

        map.put("put-file", PutFile::new);
        map.put("get-file", GetFile::new);

        map.put("send-message", SendMessage::new);
        map.put("get-messages", GetMessages::new);

        map.put("go-online", GoOnline::new);
        map.put("go-offline", GoOffline::new);

        map.put("online-users", GetOnlineUsers::new);
    }

    public static Collection<String> names() {
        return map.keySet();
    }

    public static Optional<RequestHandlerConstructor> get(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}