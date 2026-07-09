package ru.adnr.flowmanager.subscription;

import ru.adnr.flowmanager.dto.SubscriptionResponse;

import java.util.Optional;

public interface SubscriptionCacheService {

    Optional<SubscriptionResponse> findByLogin(String login);

    void save(SubscriptionResponse subscription);

    void evict(String login);
}
