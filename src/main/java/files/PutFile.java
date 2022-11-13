package files;

import infra.request.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PutFile extends RequestHandler {

  public PutFile(Request request, SharedContext ctx) {
    super(request, ctx);
  }

  private record ResponseData(String filename) {};

  public Response run() {

    try {
      var headers = request.headers();
      if (!headers.containsKey("file-extension")) {
        throw new ErrorResponse("badRequest", MsgCode.MISSING_FILE_EXTENSION_HEADER);
      }
      var extension = headers.get("file-extension");

      var filename = UUID.randomUUID() + "." + extension;

      var file = new File("./uploads/" + filename);
      if (!file.createNewFile()) {
        System.err.println("Failed to create a new file");
        throw new ErrorResponse("internal", MsgCode.IO_ERROR);
      }
      if (!file.canWrite()) {
        System.err.println("Cannot write to created file!");
        throw new ErrorResponse("internal", MsgCode.IO_ERROR);
      }

      try (var out = new FileOutputStream(file)) {
        out.write(request.body());
      }

      // TODO could also have a table in the db with file metadata (who uploaded it, when etc.)

      return responseFactory.json(new ResponseData(filename));

    } catch (ErrorResponse e) {
      return responseFactory.err(e);
    } catch (IOException e) {
      System.err.println(e);
      return responseFactory.err(new ErrorResponse("internal", MsgCode.IO_ERROR));
    }
  }
}
