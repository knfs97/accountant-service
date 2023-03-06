package account.service;

import com.google.common.cache.*;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    public static final int MAX_ATTEMPTS = 4;
    private final LoadingCache<String, Integer> attemptsCache;
    public LoginAttemptService() {
        super();
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    @NonNull
                    public Integer load(@NonNull String key) {
                        return 0;
                    }
                });
    }

    public void addUserToLoginAttemptCache(String username) {
        int attempts = getNumberOfAttempts(username);
        attempts++;
        this.attemptsCache.put(username, attempts);
    }

    public void evictUserFromLoginAttemptCache(String username) {
        attemptsCache.invalidate(username);
    }

    public int getNumberOfAttempts(String username) {
        int attempts;
        try {
            attempts = this.attemptsCache.get(username);
        } catch (ExecutionException ex) {
            attempts = 0;
        }
        return attempts;
    }
    public boolean isBlocked(String username) {
        return getNumberOfAttempts(username) > MAX_ATTEMPTS;
    }

    public void loginFailed(String username) {
        addUserToLoginAttemptCache(username);
    }

    public void loginSucceed(String username) {
        evictUserFromLoginAttemptCache(username);
    }
}
