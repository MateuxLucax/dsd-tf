package uploads;

import infra.SharedContext;
import infra.request.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class GetFile extends RequestHandler {

  public GetFile(Request request, SharedContext ctx) {
    super(request, ctx);
  }

  private record RequestData(String filename) {};

  public Response run() throws SQLException {

    try {
      var data = readJson(RequestData.class);
      var filename = data.filename();

      var file = new File("./uploads/" + filename);
      if (!file.exists()) {
        throw new ErrorResponse("badRequest", MsgCode.FILE_DOES_NOT_EXISTS);
      }
      if (!file.canRead()) {
        throw new ErrorResponse("internal", MsgCode.IO_ERROR);
      }

      var fileContents = new byte[(int) file.length()];

      try (var in = new FileInputStream(file)) {
        in.read(fileContents);
      }

      return new Response(true, "", fileContents);

    } catch (ErrorResponse e) {
      return responseFactory.err(e);
    } catch (IOException e) {
      System.err.println(e);
      return responseFactory.err(new ErrorResponse("internal", MsgCode.IO_ERROR));
    }
  }
}
