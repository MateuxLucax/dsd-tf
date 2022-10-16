package meta;

import infra.*;

import java.util.ArrayList;

// to check if the client code has all the necessary codes

public class GetAllErrorCodes extends RequestHandler {

    public boolean tokenRequired() {
        return false;
    }

    public GetAllErrorCodes(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() {
        var codes = new ArrayList<String>();
        for (var code : ErrCode.values()) {
            codes.add(code.toString());
        }
        return responseFactory.json(codes);
    }
}
