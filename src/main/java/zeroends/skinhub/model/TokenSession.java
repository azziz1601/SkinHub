package zeroends.skinhub.model;

import java.util.UUID;

public class TokenSession {
    
    private final String sessionId;
    private final UUID playerUuid;
    private final long expiryTimestamp;

    public TokenSession(String sessionId, UUID playerUuid, long expiryTimestamp) {
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.expiryTimestamp = expiryTimestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }
}