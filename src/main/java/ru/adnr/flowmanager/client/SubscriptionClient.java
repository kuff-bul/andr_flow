package ru.adnr.flowmanager.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.adnr.flowmanager.dto.SubscriptionResponse;

@FeignClient(name = "subscription-service")
public interface SubscriptionClient {

    @GetMapping("/api/subscriptions/{login}")
    SubscriptionResponse getSubscription(@PathVariable String login);
}
