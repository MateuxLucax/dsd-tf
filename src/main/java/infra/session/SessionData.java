package infra.session;

// TODO make thread-safe, if necessary

// could add something like Map<String, SessionVariable> for sharing data among handlers in the session, like $_SESSION in php
// where SessionVariable = record(String name, SessionVariableType type, Object value)
// where SessionVariableType = enum(int, long, double, float, char, String)
// and a polymorphic 'make' method SessionVariable.make(String name, int value), SessionVariable.make(String name, long value) etc.
// getIntValue(), getLongValue(), getStringValue() which throw RuntimeException if the value is not of the corresponding type

import java.time.Instant;

public class SessionData {

    private final Instant createdAt;
    private final long userId;
    private final String token;

    public SessionData(String token, long userId) {
        this.token = token;
        this.userId = userId;
        createdAt = Instant.now();
    }

    public long getUserId() { return userId; }

    public String getToken() { return token; }

    public boolean isExpired()
    { return Instant.now().minus(SessionManager.SESSION_DURATION).isAfter(createdAt); }

    public Instant createdAt() {
        return createdAt;
    }
}
