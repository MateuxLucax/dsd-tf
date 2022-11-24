package meta;

import infra.SharedContext;
import infra.request.OperationLookup;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

public class GetOperationIndex extends RequestHandler {

    public boolean tokenRequired() { return false; }

    public GetOperationIndex(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
        var operations = OperationLookup.names();
        return responseFactory.json(operations);
    }
}
