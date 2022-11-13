package infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helpers.RecordTypeAdapterFactory;
import infra.request.ResponseFactory;
import infra.session.SessionManager;
import live.LiveSockets;

public class SharedContext {

    private final Gson gson;
    private final SessionManager mgr;
    private final ResponseFactory resFac;
    private final LiveSockets liveSockets;

    public SharedContext() {
        this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .setPrettyPrinting()
            .create();

        this.resFac = new ResponseFactory(gson);

        this.mgr = new SessionManager();

        this.liveSockets = new LiveSockets();
    }

    public SessionManager sessionManager() {
        return mgr;
    }

    public ResponseFactory responseFactory() {
        return this.resFac;
    }

    public LiveSockets getLiveSockets() {
        return this.liveSockets;
    }

    public Gson gson() {
        return gson;
    }
}
