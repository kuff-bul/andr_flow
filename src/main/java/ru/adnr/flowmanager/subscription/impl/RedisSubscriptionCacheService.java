package ru.adnr.flowmanager.subscription.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.adnr.flowmanager.config.SubscriptionProperties;
import ru.adnr.flowmanager.dto.SubscriptionResponse;
import ru.adnr.flowmanager.subscription.SubscriptionCacheService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriptionCacheService implements SubscriptionCacheService {

    private static final String KEY_PREFIX = "subscription:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SubscriptionProperties subscriptionProperties;

    @Override
    public Optional<SubscriptionResponse> findByLogin(String login) {
        try {
            String payload = redisTemplate.opsForValue().get(key(login));
            if (payload == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(payload, SubscriptionResponse.class));
        } catch (RedisConnectionFailureException exception) {
            log.warn("Redis unavailable while reading subscription cache. login={}", login, exception);
            return Optional.empty();
        } catch (JsonProcessingException exception) {
            log.warn("Invalid subscription cache payload. login={}", login, exception);
            evict(login);
            return Optional.empty();
        }
    }

    @Override
    public void save(SubscriptionResponse subscription) {
        try {
            redisTemplate.opsForValue().set(
                    key(subscription.login()),
                    objectMapper.writeValueAsString(subscription),
                    subscriptionProperties.cacheTtl()
            );
        } catch (RedisConnectionFailureException exception) {
            log.warn("Redis unavailable while writing subscription cache. login={}", subscription.login(), exception);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize subscription cache payload. login={}", subscription.login(), exception);
        }
    }

    @Override
    public void evict(String login) {
        try {
            redisTemplate.delete(key(login));
        } catch (RedisConnectionFailureException exception) {
            log.warn("Redis unavailable while evicting subscription cache. login={}", login, exception);
        }
    }

    private String key(String login) {
        return KEY_PREFIX + login;
    }
}
