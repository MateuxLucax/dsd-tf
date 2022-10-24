package friends;

import infra.*;

import java.sql.SQLException;
import java.util.ArrayList;

// just return all of them

// TODO also return last message date when we implement messages

public class GetFriends extends RequestHandler {

    private record UserData(long id, String username, String fullname) {}

    public GetFriends(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    public Response run() throws SQLException {
        try (var connection = Database.getConnection()) {
            var sql =
                "SELECT u.id, u.username, u.fullname " +
                "FROM friends f " +
                "JOIN users u ON u.id = f.their_id " +
                "WHERE your_id = ?";
            var stmt = connection.prepareStatement(sql);
            stmt.setLong(1, getUserId());

            var result = stmt.executeQuery();
            var friends = new ArrayList<UserData>();
            while (result.next()) {
                var id = result.getLong("id");
                var username = result.getString("username");
                var fullname = result.getString("fullname");
                friends.add(new UserData(id, username, fullname));
            }

            return responseFactory.json(friends);
        }
    }
}
