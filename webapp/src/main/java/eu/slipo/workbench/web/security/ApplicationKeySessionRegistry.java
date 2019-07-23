package eu.slipo.workbench.web.security;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;

@Component
public class ApplicationKeySessionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationKeySessionRegistry.class);

    /**
     * Session cleanup interval in milliseconds
     */
    private static final long SESSION_CLEANUP_INTERVAL = 5 * 60 * 1000;

    /**
     * Session interval in seconds
     */
    private static final long SESSION_INTERVAL = 15 * 60;

    private static class Session {

        public String applicationKey;
        public String sessionToken;
        public ZonedDateTime updated = ZonedDateTime.now();

    }

    private Map<String, Session> keyMap = new HashMap<String, Session>();
    private Map<String, Session> tokenMap = new HashMap<String, Session>();

    public String keyToSessionToken(ApplicationKeyRecord key) {
        return this.keyToSessionToken(key.getKey());
    }

    public String keyToSessionToken(String key) {
        try {
            synchronized (this) {
                Session session = this.keyMap.get(key);
                if (session == null) {
                    session = new Session();
                    session.applicationKey = key;
                    session.sessionToken = UUID.randomUUID().toString();

                    this.keyMap.put(key, session);
                    this.tokenMap.put(session.sessionToken, session);
                } else {
                    session.updated = ZonedDateTime.now();
                }

                return session.sessionToken;
            }
        } catch (Exception ex) {
            logger.error("Failed to create session token for API call", ex);
        }

        return null;
    }

    public String sessionTokenToKey(String token) {
        try {
            synchronized (this) {
                Session session = this.tokenMap.get(token);
                if (session != null) {
                    session.updated = ZonedDateTime.now();

                    return session.applicationKey;
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to verify session token for API call", ex);
        }

        return null;
    }

    /**
     * Removes all session tokens not used for the last 15 minutes
     */
    @Scheduled(fixedRate = SESSION_CLEANUP_INTERVAL, initialDelay = 5000L)
    public void invalidateSessions() {
        try {
            synchronized (this) {
                final ZonedDateTime now = ZonedDateTime.now();
                final List<Session> evict = new ArrayList<Session>();

                this.tokenMap.values().stream().forEach(session -> {
                    if (session.updated.plusSeconds(SESSION_INTERVAL).compareTo(now) < 0) {
                        evict.add(session);
                    }
                });

                evict.stream().forEach(session -> {
                    this.keyMap.remove(session.applicationKey);
                    this.tokenMap.remove(session.sessionToken);
                });
            }
        } catch (Exception ex) {
            logger.error("Failed to cleanup session tokens", ex);
        }
    }

}
