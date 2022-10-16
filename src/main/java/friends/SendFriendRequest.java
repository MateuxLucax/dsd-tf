package friends;

import infra.*;

import java.sql.SQLException;

public class SendFriendRequest extends RequestHandler {

    public SendFriendRequest(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public record RequestData(long userId) {}

    @Override
    public Response run() throws SQLException {

        var conn = Database.getConnection();
        try {
            conn.setAutoCommit(false);

            var data = readJson(RequestData.class);

            var sourceUserId = ctx.sessionManager().getSessionData( request.headers().get("token") ).getUserId();
            var targetUserId = data.userId;

            if (sourceUserId == targetUserId) {
                throw new ErrorResponse("badRequest", ErrCode.FRIEND_REQUEST_TO_YOURSELF);
            }

            // check if user exists
            {
                var sql = "SELECT 1 FROM users WHERE id = ?";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, data.userId);
                var res = stmt.executeQuery();
                if (!res.next()) {
                    throw new ErrorResponse("badRequest", ErrCode.NO_USER_WITH_GIVEN_ID);
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
                    if (senderId == ctx.sessionManager().getSessionData(token).getUserId()) {
                        throw new ErrorResponse("badRequest", ErrCode.YOU_ALREADY_SENT_FRIEND_REQUEST);
                    } else {
                        throw new ErrorResponse("badRequest", ErrCode.THEY_ALREADY_SENT_FRIEND_REQUEST);
                    }
                }
            }

            // finally send the request
            {
                var sql = "INSERT INTO friend_requests (sender_id, receiver_id, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, sourceUserId);
                stmt.setLong(2, targetUserId);
                var n = stmt.executeUpdate();
                if (n != 1) {
                    throw new ErrorResponse("internal", ErrCode.FAILED_TO_SEND_FRIEND_REQUEST);
                }
            }

            conn.commit();
            return responseFactory.json(MessageCodeBody.from(ErrCode.SENT_FRIEND_REQUEST_SUCCESSFULLY));

        } catch (ErrorResponse e) {
            conn.rollback();
            return responseFactory.err(e);
        } finally {
            conn.close();
        }
    }
}
