package ru.adnr.flowmanager.subscription;

import ru.adnr.flowmanager.dto.SubscriptionResponse;

public interface SubscriptionCacheService {

    SubscriptionResponse findByLogin(String login);

    void evict(String login);
}
