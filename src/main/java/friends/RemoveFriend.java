package friends;

import infra.Database;
import infra.SharedContext;
import infra.request.ErrorResponse;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;

import java.sql.SQLException;

public class RemoveFriend extends RequestHandler {

  private record RequestData(long friendId) {}
  
  public RemoveFriend(Request request, SharedContext ctx) {
    super(request, ctx);
  }

  public Response run() throws SQLException, InterruptedException {
    try (var conn = Database.getConnection()) {
      var userId = getUserId();

      var friendId = readJson(RequestData.class).friendId;

      var sql =
        "DELETE FROM friends " +
        "WHERE (your_id, their_id) = (?, ?)" +
        "   OR (your_id, their_id) = (?, ?)";
      var stmt = conn.prepareStatement(sql);
      stmt.setLong(1, userId);   stmt.setLong(2, friendId);
      stmt.setLong(3, friendId); stmt.setLong(4, userId);
      stmt.executeUpdate();
      return responseFactory.justOk();

    } catch (ErrorResponse e) {
      return responseFactory.err(e);
    }
  }
}