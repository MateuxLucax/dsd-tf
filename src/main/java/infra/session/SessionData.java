package infra.session;

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

    public boolean isExpired() {
        return Instant.now().minus(SessionManager.SESSION_DURATION).isAfter(createdAt);
    }
}
