package user;

import infra.*;
import infra.request.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class SearchUsers extends RequestHandler {

    public SearchUsers(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private static final int RESULTS_PER_PAGE = 20;

    private record RequestData(String query, int page) {}

    private record UserData(
        long id,
        String username,
        String fullname,
        FriendshipStatus friendshipStatus
    ) {}

    @Override
    public Response run() throws SQLException {
        try (var conn = Database.getConnection()) {
            var req = readJson(RequestData.class);

            if (req.page <= 0) {
                throw new ErrorResponse("badRequest", MsgCode.INVALID_PAGE_NUMBER);
            }

            var userId = getUserId();

            var sql =
                "SELECT u.id, " +
                "       u.username, " +
                "       u.fullname," +
                "       EXISTS (SELECT 1 " +
                "                 FROM friends " +
                "                WHERE (your_id = ? AND their_id = u.id) " +
                "                   OR (your_id = u.id AND their_id = ?)" +
                "       ) AS is_friend," +
                "       EXISTS (SELECT 1" +
                "                 FROM friend_requests " +
                "                WHERE (sender_id = ? AND receiver_id = u.id)" +
                "       ) AS has_received_friend_request, " +
                "       EXISTS (SELECT 1" +
                "                 FROM friend_requests" +
                "                WHERE (sender_id = u.id AND receiver_id = ?)" +
                "       ) AS has_sent_friend_request " +
                "  FROM users u " +
                " WHERE u.username like ? " +
                "   AND u.id <> ? " +
                " LIMIT ? " +
                " OFFSET ?";
            var stmt = conn.prepareStatement(sql);

            // TODO is that the right way to escape a %?
            var i = 1;

            stmt.setLong(i++, userId); // friends.your_id
            stmt.setLong(i++, userId); // friends.their_id
            stmt.setLong(i++, userId); // friend_requests.sender_id
            stmt.setLong(i++, userId); // friend_requests.receiver_id

            stmt.setString(i++, "%" + req.query.replace("%", "\\%") + "%");
            stmt.setLong(i++, userId);
            stmt.setInt(i++, RESULTS_PER_PAGE);
            stmt.setInt(i++, (req.page - 1) * RESULTS_PER_PAGE);

            var res = stmt.executeQuery();

            var users = new ArrayList<UserData>();
            while (res.next()) {
                var id = res.getInt("id");
                var username = res.getString("username");
                var fullname = res.getString("fullname");

                var isFriend = res.getBoolean("is_friend");
                var sentRequest = res.getBoolean("has_sent_friend_request");
                var receivedRequest = res.getBoolean("has_received_friend_request");

                var status = FriendshipStatus
                    .from(isFriend, sentRequest, receivedRequest)
                    .orElseThrow(() -> {
                        System.err.printf("FriendshipStatus.from invalid booleans, at most one can be true (isFriend: %s, sentRequest: %s, receivedRequest: %s)", isFriend, sentRequest, receivedRequest);
                        return new ErrorResponse("internal", MsgCode.INTERNAL);
                    });

                var user = new UserData(id, username, fullname, status);
                users.add(user);
            }

            return responseFactory.json(users);
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
