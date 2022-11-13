package friends;

import infra.*;
import infra.request.Request;
import infra.request.RequestHandler;
import infra.request.Response;
import infra.request.SharedContext;

import java.sql.SQLException;
import java.util.ArrayList;

public class GetFriendRequests extends RequestHandler {

    public GetFriendRequests(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    // Just returns all friends requests involving the user
    // The front-end can do the filtering, ordering etc.

    private record UserData(int id, String username, String fullname) {}

    private record FriendRequestData(UserData from, UserData to, String createdAt, String updatedAt) {}

    @Override
    public Response run() throws SQLException {

        try (var conn = Database.getConnection()) {

            var session = ctx.sessionManager().getSessionData(request.headers().get("token"));
            var userId = session.getUserId();

            var sql =
                "SELECT fr.created_at" +
                "     , fr.updated_at" +
                "     , us.id AS sender_id" +
                "     , us.username AS sender_username" +
                "     , us.fullname AS sender_fullname" +
                "     , ur.id AS receiver_id" +
                "     , ur.username AS receiver_username" +
                "     , ur.fullname AS receiver_fullname" +
                "  FROM friend_requests fr" +
                "  JOIN users us ON us.id = fr.sender_id" +
                "  JOIN users ur ON ur.id = fr.receiver_id" +
                " WHERE (fr.sender_id = ? OR fr.receiver_id = ?)";

            var stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);

            var res = stmt.executeQuery();

            var friendRequestList = new ArrayList<FriendRequestData>();

            while (res.next()) {
                UserData sender;
                {
                    var id = res.getInt("sender_id");
                    var username = res.getString("sender_username");
                    var fullname = res.getString("sender_fullname");
                    sender = new UserData(id, username, fullname);
                }

                UserData receiver;
                {
                    var id = res.getInt("receiver_id");
                    var username = res.getString("receiver_username");
                    var fullname = res.getString("receiver_fullname");
                    receiver = new UserData(id, username, fullname);
                }

                var createdAtString = res.getTimestamp("created_at").toInstant().toString();

                var updatedAt = res.getTimestamp("updated_at");
                var updatedAtString = updatedAt != null ? updatedAt.toInstant().toString() : null;

                var friendRequest = new FriendRequestData(sender, receiver, createdAtString, updatedAtString);
                friendRequestList.add(friendRequest);
            }

            return responseFactory.json(friendRequestList);
        }
    }
}
