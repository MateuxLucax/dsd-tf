package friends;

import infra.*;
import infra.request.*;

import java.sql.SQLException;

public class SendFriendRequest extends RequestHandler {

    public SendFriendRequest(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public record RequestData(long userId) {}

    @Override
    public Response run() throws SQLException, InterruptedException {

        var conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);

            var data = readJson(RequestData.class);

            var sourceUserId = getUserId();
            var targetUserId = data.userId;

            if (sourceUserId == targetUserId) {
                throw new ErrorResponse("badRequest", MsgCode.FRIEND_REQUEST_TO_YOURSELF);
            }

            // check if user exists
            {
                var sql = "SELECT 1 FROM users WHERE id = ?";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, data.userId);
                var res = stmt.executeQuery();
                if (!res.next()) {
                    throw new ErrorResponse("badRequest", MsgCode.NO_USER_WITH_GIVEN_ID);
                }
            }

            // check if there's no pending friend request already
            {
                var sql =
                    "SELECT sender_id" +
                    "     , receiver_id" +
                    "  FROM friend_requests" +
                    " WHERE (sender_id = ? AND receiver_id = ?)" +
                    "    OR (sender_id = ? AND receiver_id = ?)";
                var stmt = conn.prepareStatement(sql);

                stmt.setLong(1, sourceUserId); stmt.setLong(2, targetUserId);
                stmt.setLong(3, targetUserId); stmt.setLong(4, sourceUserId);

                var res = stmt.executeQuery();
                if (res.next()) {
                    var senderId = res.getLong("sender_id");

                    var token = request.headers().get("token");
                    if (senderId == getUserId()) {
                        throw new ErrorResponse("badRequest", MsgCode.YOU_ALREADY_SENT_FRIEND_REQUEST);
                    } else {
                        throw new ErrorResponse("badRequest", MsgCode.THEY_ALREADY_SENT_FRIEND_REQUEST);
                    }
                }
            }

            // verify they aren't already friends
            {
                var sql = "SELECT 1 FROM friends WHERE (your_id, their_id) = (?, ?) OR (your_id, their_id) = (?, ?)";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, sourceUserId); stmt.setLong(2, targetUserId);
                stmt.setLong(3, targetUserId); stmt.setLong(4, sourceUserId);
                var result = stmt.executeQuery();
                if (result.next()) {
                    throw new ErrorResponse("badRequest", MsgCode.ALREADY_FRIENDS);
                }
            }

            // finally send the request
            {
                var sql =
                    "INSERT INTO friend_requests (sender_id, receiver_id, created_at) VALUES " +
                    "(?, ?, CURRENT_TIMESTAMP)";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, sourceUserId);
                stmt.setLong(2, targetUserId);
                var n = stmt.executeUpdate();
                if (n != 1) {
                    throw new ErrorResponse("internal", MsgCode.FAILED_TO_SEND_FRIEND_REQUEST);
                }
            }

            conn.commit();
            return responseFactory.json(MessageCodeBody.from(MsgCode.SENT_FRIEND_REQUEST_SUCCESSFULLY));

        } catch (ErrorResponse e) {
            conn.rollback();
            return responseFactory.err(e);
        } finally {
            conn.close();
        }
    }
}
