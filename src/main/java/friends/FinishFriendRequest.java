package friends;

// Finish means accepting OR denying
// In both cases, the friend request is removed

import infra.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class FinishFriendRequest extends RequestHandler {

    // The user sending the request is the receiver
    // so the request only needs the ID of the sender

    private record RequestBody(long senderId, boolean accepted) {}

    public FinishFriendRequest(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
        var connection = Database.getConnection();
        connection.setAutoCommit(false);

        try {

            var requestBody = readJson(RequestBody.class);
            var senderId = requestBody.senderId();
            var accepted = requestBody.accepted();

            var token = request.headers().get("token");
            var receiverId = ctx.sessionManager().getSessionData(token).getUserId();

            if (senderId == receiverId) {
                throw new ErrorResponse("badRequest", MsgCode.FRIEND_REQUEST_TO_YOURSELF);
            }

            {
                var sql =
                    "DELETE " +
                    "FROM friend_requests " +
                    "WHERE sender_id = ?" +
                    "AND receiver_id = ?";
                var statement = connection.prepareStatement(sql);
                statement.setLong(1, senderId);
                statement.setLong(2, receiverId);
                int numDeleted = statement.executeUpdate();
                if (numDeleted == 0) {
                    throw new ErrorResponse("badRequest", MsgCode.FRIEND_REQUEST_NOT_FOUND);
                }
            }

            if (accepted) {
                var sql =
                    "INSERT INTO friends (friend1_id, friend2_id, created_at) " +
                    "VALUES (?, ?, ?)";
                var statement = connection.prepareStatement(sql);
                statement.setLong(1, senderId);
                statement.setLong(2, receiverId);
                statement.setTimestamp(3, Timestamp.from(Instant.now()));
                int numInserted = statement.executeUpdate();
                if (numInserted == 0) {
                    throw new ErrorResponse("internal", MsgCode.FAILED_TO_FINISH_FRIEND_REQUEST);
                }
            }

            connection.commit();

            return responseFactory.json(MessageCodeBody.from(MsgCode.FINISHED_FRIEND_REQUEST_SUCCESSFULLY));

        } catch (ErrorResponse e) {
            connection.rollback();
            connection.close();
            return responseFactory.err(e);
        }
    }
}
