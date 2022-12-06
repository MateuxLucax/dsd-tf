package messages;

import eventqueue.events.ChatMessageSentEvent;
import infra.Database;
import infra.SharedContext;
import infra.request.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class SendMessage extends RequestHandler {

    public SendMessage(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestData(
        long to,
        String textContents,
        String fileReference
    ) {}

    private record ResponseData(
        long id,
        String sentAt
    ) {}

    public Response run() throws SQLException {
        Connection connection = null;

        try {
            connection = Database.getConnection();
            connection.setAutoCommit(false);

            var data = readJson(RequestData.class);
            var text = data.textContents;
            var fileRef = data.fileReference;
            if (text == null && fileRef == null) {
                throw new ErrorResponse("badRequest", MsgCode.MALFORMED_MESSAGE);
            }
            if (text != null && fileRef != null) {
                throw new ErrorResponse("badRequest", MsgCode.MALFORMED_MESSAGE);
            }

            var senderId = getUserId();
            var receiverId = data.to;

            // block sending messages to non-friends
            {
                var sql = "SELECT 1 FROM friends WHERE your_id = ? AND their_id = ?";
                var stmt = connection.prepareStatement(sql);
                stmt.setLong(1, senderId);
                stmt.setLong(2, receiverId);
                var result = stmt.executeQuery();
                if (!result.next()) {
                    throw new ErrorResponse("badRequest", MsgCode.NOT_FRIENDS);
                }
            }


            var sentAt = Instant.now();

            {
                var sql = "INSERT INTO messages (sender_id, receiver_id, text_contents, file_reference, sent_at) VALUES (?, ?, ?, ?, ?)";
                var stmt = connection.prepareStatement(sql);
                stmt.setLong(1, senderId);
                stmt.setLong(2, receiverId);
                stmt.setString(3, text);
                stmt.setString(4, fileRef);
                stmt.setTimestamp(5, Timestamp.from(sentAt));
                var n = stmt.executeUpdate();
                if (n == 0) {
                    throw new ErrorResponse("internal", MsgCode.INTERNAL);
                }
            }

            Long messageId = null;

            {
                var sql = "SELECT MAX(id) FROM messages WHERE (sender_id, receiver_id) = (?, ?);";
                var stmt = connection.prepareStatement(sql);
                stmt.setLong(1, senderId);
                stmt.setLong(2, receiverId);
                var res = stmt.executeQuery();
                if (!res.next()) {
                    throw new ErrorResponse("internal", MsgCode.INTERNAL);
                }
                messageId = res.getLong(1);
            }

            connection.commit();

            var event = new ChatMessageSentEvent(messageId, senderId, receiverId, data.textContents, data.fileReference, sentAt);
            ctx.eventQueue().enqueue(event);

            var response = new ResponseData(messageId, sentAt.toString());
            return responseFactory.json(response);

        } catch (ErrorResponse e) {
            if (!connection.getAutoCommit())
                connection.rollback();
            return responseFactory.err(e);
        } finally {
            if (connection != null)
                connection.close();
        }
    }
}
