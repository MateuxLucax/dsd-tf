package infra;

import user.Whoami;
import user.CreateSession;
import user.CreateUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OperationLookup {

    @FunctionalInterface
    public interface RequestHandlerConstructor {
        RequestHandler cons(Request req, ResponseWriter res, SharedContext ctx);
    }

    private static final Map<String, RequestHandlerConstructor> map;

    static {
        map = new HashMap<>();
        map.put("create-user", CreateUser::new);
        map.put("create-session", CreateSession::new);
        map.put("whoami", Whoami::new);
    }

    public static Optional<RequestHandlerConstructor> get(String operation) {
        return Optional.ofNullable(map.get(operation));
    }
}