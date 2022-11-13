package files;

import infra.request.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class GetFile extends RequestHandler {

    public GetFile(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestData(String filename) {}

    public Response run() throws SQLException {
        try {
            var filename = readJson(RequestData.class).filename;
            var file = new File("./uploads/" + filename);
            if (!file.exists() || !file.isFile()) {
                throw new ErrorResponse("badRequest", MsgCode.FILE_DOES_NOT_EXISTS);
            }
            if (!file.canRead()) {
                throw new ErrorResponse("internal", MsgCode.IO_ERROR);
            }
            try {
                var in = new FileInputStream(file);
                var fileContents = new byte[(int) file.length()];
                in.read(fileContents);
                return new Response(true, "", fileContents);
            } catch (IOException e) {
                throw new ErrorResponse("internal", MsgCode.IO_ERROR);
            }
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
