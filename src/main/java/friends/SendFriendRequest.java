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
                throw new ErrorResponse("badRequest", "Cannot send a friend request to yourself!");
            }

            // check if user exists
            {
                var sql = "SELECT 1 FROM users WHERE id = ?";
                var stmt = conn.prepareStatement(sql);
                stmt.setLong(1, data.userId);
                var res = stmt.executeQuery();
                if (!res.next()) {
                    throw new ErrorResponse("badRequest", "No user with given id");
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
                        throw new ErrorResponse("badRequest", "You already sent a friend request to this user!");
                    } else {
                        throw new ErrorResponse("badRequest", "This user has already sent a friend request to you!");
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
                    throw new ErrorResponse("internal", "Failed to send friend request.");
                }
            }

            conn.commit();
            return responseFactory.json(new MessageBody("Sent friend request successfully."));

        } catch (ErrorResponse e) {
            conn.rollback();
            return responseFactory.err(e);
        } finally {
            conn.close();
        }
    }
}
