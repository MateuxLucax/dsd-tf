package infra;

import com.google.gson.Gson;

public class ResponseFactory {

    private final Gson gson;

    public ResponseFactory(Gson gson) {
        this.gson = gson;
    }

    public Response err(ErrorResponse e) {
        return new Response(false, e.getKind(), gson.toJson(e.toBody()).getBytes());
    }

    public Response err(String kind, String msg) {
        return err(new ErrorResponse(kind, msg));
    }

    public Response json(Object o) {
        return new Response(true, "", gson.toJson(o).getBytes());
    }
}
