package infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helpers.RecordTypeAdapterFactory;

public class SharedContext {

    private final Gson gson;
    private final SessionManager mgr;

    public SharedContext() {
        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        this.gson = gsonb.create();

        this.mgr = new SessionManager();
    }

    public SessionManager sessionManager() {
        return mgr;
    }

    public Gson gson() {
        return gson;
    }
}
