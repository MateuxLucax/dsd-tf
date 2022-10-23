package user;

import infra.*;

import java.sql.SQLException;
import java.util.ArrayList;

public class SearchUsers extends RequestHandler {

    public SearchUsers(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private static final int RESULTS_PER_PAGE = 20;

    private record RequestData(String search, int page) {}

    private record UserData(int id, String username, String fullname) {}

    @Override
    public Response run() throws SQLException {
        try (var conn = Database.getConnection()) {
            var req = readJson(RequestData.class);

            if (req.page <= 0) {
                throw new ErrorResponse("badRequest", MsgCode.INVALID_PAGE_NUMBER);
            }

            // TODO test pagination

            // TODO return more info: whether is friend, friend request status, etc.

            var sql = "SELECT id, username, fullname FROM users WHERE username like ? LIMIT ? OFFSET ?";
            var stmt = conn.prepareStatement(sql);

            // TODO is that the right way to escape a %?
            stmt.setString(1, "%" + req.search.replace("%", "\\%") + "%");
            stmt.setInt(2, RESULTS_PER_PAGE);
            stmt.setInt(3, (req.page - 1) * RESULTS_PER_PAGE);

            var res = stmt.executeQuery();

            var users = new ArrayList<UserData>();
            while (res.next()) {
                var id = res.getInt("id");
                var username = res.getString("username");
                var fullname = res.getString("fullname");
                var user = new UserData(id, username, fullname);
                users.add(user);
            }

            return responseFactory.json(users);
        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
