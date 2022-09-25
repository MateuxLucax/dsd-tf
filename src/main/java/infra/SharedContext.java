package infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helpers.RecordTypeAdapterFactory;

public class SharedContext {

    private final Gson gson;

    // TODO session manager

    public SharedContext() {
        var gsonb = new GsonBuilder();
        gsonb.registerTypeAdapterFactory(new RecordTypeAdapterFactory());
        this.gson = gsonb.create();
    }

    public Gson gson() {
        return gson;
    }
}
