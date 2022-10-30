package user;

import infra.*;

import java.sql.SQLException;

public class EditUser extends RequestHandler {

    public EditUser(Request request, SharedContext ctx) {
        super(request, ctx);
    }

    private record RequestData(
        String newFullname,
        String newPassword
    ) {}

    public Response run() throws SQLException {
        try (var connection = Database.getConnection()) {

            var data = readJson(RequestData.class);

            var sql = "UPDATE users SET fullname = ?, password = ? WHERE id = ?";
            var statement = connection.prepareStatement(sql);
            var userId = getUserId();
            statement.setString(1, data.newFullname);
            statement.setString(2, data.newPassword);
            statement.setLong(3, userId);
            var numUpdated = statement.executeUpdate();
            if (numUpdated == 0) {
                // It could be because there's no user with the given ID
                // but then how would there be a session for this user?
                // Because of a bug: when the user is deleted all of its existing sessions should be deleted, but they didn't (at least not fast enough)
                // So if this happens, it's a bug
                System.err.println("EditUser: update statement had no effect, id " + userId);
                throw new ErrorResponse("internal", MsgCode.INTERNAL);
            }

            return responseFactory.justOk();

        } catch (ErrorResponse e) {
            return responseFactory.err(e);
        }
    }
}
