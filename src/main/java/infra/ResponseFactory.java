package infra;

import com.google.gson.Gson;

public class ResponseFactory {

    private final Gson gson;

    public ResponseFactory(Gson gson) {
        this.gson = gson;
    }

    public Response err(ErrorResponse e) {
        return new Response(false, e.getKind(), gson.toJson(e.toResponseBody()).getBytes());
    }

    public Response err(String kind, MsgCode code) {
        return err(new ErrorResponse(kind, code));
    }

    public Response json(Object o) {
        return new Response(true, "", gson.toJson(o).getBytes());
    }

    public Response justOk() {
        return new Response(true, "", new byte[]{});
    }
}
