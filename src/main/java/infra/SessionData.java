package infra;

// TODO make thread-safe, if necessary

// could add something like Map<String, SessionVariable> for sharing data among handlers in the session, like $_SESSION in php
// where SessionVariable = record(String name, SessionVariableType type)
// where SessionVariableType = enum(int, long, double, float, char, String)
// and a polymorphic 'make' method SessionVariable.make(String name, int value), SessionVariable.make(String name, long value) etc.
// getIntValue(), getLongValue(), getStringValue() which throw RuntimeException if the value is not of the corresponding type

public class SessionData {

    private final long userId;
    private final String token;

    public SessionData(String token, long userId) {
        this.token = token;
        this.userId = userId;
    }

    public long getUserId() { return userId; }

    public String getToken() { return token; }
}
