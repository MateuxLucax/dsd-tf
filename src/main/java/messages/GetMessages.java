package messages;

import infra.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class GetMessages extends RequestHandler {

    public GetMessages(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestData(
        long friendId,
        long before,
        int limit
    ) {}

    private record MessageData(
        long id,
        long userId,
        String sentAt,
        String textContents,
        String fileReference
    ) {}

    public Response run() throws SQLException {
        try (var connection = Database.getConnection()) {

            var reqdata = readJson(RequestData.class);

            var limit = Math.max(1, Math.min(reqdata.limit, 100));

            var yourId = getUserId();
            var theirId = reqdata.friendId();

            // first check if is friend
            {
                var sql = "SELECT 1 FROM friends WHERE your_id = ? AND their_id = ?";
                var stmt = connection.prepareStatement(sql);
                stmt.setLong(1, yourId);
                stmt.setLong(2, theirId);
                var result = stmt.executeQuery();
                if (!result.next()) {
                    throw new ErrorResponse("badRequest", MsgCode.NOT_FRIENDS);
                }
            }

            // retrieve the messages
            {
                var sql =
                    "SELECT id, sender_id, sent_at, text_contents, file_reference " +
                    "  FROM messages " +
                    " WHERE ((sender_id, receiver_id) = (?, ?) OR (sender_id, receiver_id) = (?, ?)) ";
                if (reqdata.before() > 0) {
                    sql += "AND id <= ? ";
                }
                sql += "ORDER BY id DESC LIMIT ?";
                var stmt = connection.prepareStatement(sql);

                var i = 1;

                stmt.setLong(i++, yourId); stmt.setLong(i++, theirId);
                stmt.setLong(i++, theirId); stmt.setLong(i++, yourId);
                if (reqdata.before() > 0) {
                    stmt.setLong(i++, reqdata.before());
                }
                stmt.setLong(i++, limit);

                var messages = new ArrayList<MessageData>();

                var result = stmt.executeQuery();
                while (result.next()) {
                    var id = result.getLong("id");
                    var userId = result.getLong("sender_id");
                    var sentAt = result.getTimestamp("sent_at").toInstant().toString();
                    var text = result.getString("text_contents");
                    var fileRef = result.getString("file_reference");

                    var message = new MessageData(id, userId, sentAt, text, fileRef);
                    messages.add(message);
                }

                return responseFactory.json(messages);
            }
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
