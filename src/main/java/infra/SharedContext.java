package infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import events.EventQueue;
import helpers.RecordTypeAdapterFactory;
import infra.request.ResponseFactory;
import infra.session.SessionManager;

public class SharedContext {

    private final Gson gson;
    private final SessionManager mgr;
    private final ResponseFactory resFac;
    private final EventQueue eventQueue;

    public SharedContext() {
        this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .setPrettyPrinting()
            .create();

        this.resFac = new ResponseFactory(gson);

        this.mgr = new SessionManager();

        this.eventQueue = new EventQueue();
    }

    public SessionManager sessionManager() {
        return mgr;
    }

    public ResponseFactory responseFactory() {
        return this.resFac;
    }

    public Gson gson() {
        return gson;
    }

    public EventQueue eventQueue() {
        return this.eventQueue;
    }
}
