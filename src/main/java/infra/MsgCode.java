package infra;

import java.util.Optional;

public enum MsgCode {

    // general
    INTERNAL,
    FAILED_TO_PARSE_JSON,
    NO_USER_WITH_GIVEN_ID,
    UNKNOWN_OPERATION,
    TOKEN_EXPIRED,
    MALFORMED_REQUEST,
    NO_RESPONSE,

    // create-user
    FAILED_TO_CREATE_USER,
    USERNAME_IN_USE,
    USER_CREATED_SUCCESSFULLY,

    // create-session
    INCORRECT_CREDENTIALS,

    // search-users
    INVALID_PAGE_NUMBER,

    // send-friend-request
    FRIEND_REQUEST_TO_YOURSELF,
    YOU_ALREADY_SENT_FRIEND_REQUEST,
    THEY_ALREADY_SENT_FRIEND_REQUEST,
    FAILED_TO_SEND_FRIEND_REQUEST,
    SENT_FRIEND_REQUEST_SUCCESSFULLY,
    ;

    public static Optional<MsgCode> from(String name) {
        for (var code : values())
            if (code.name().equals(name))
                return Optional.of(code);
        return Optional.empty();
    }

}
