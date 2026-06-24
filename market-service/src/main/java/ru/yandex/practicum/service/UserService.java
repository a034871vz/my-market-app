package ru.yandex.practicum.service;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.config.MarketUserDetails;

@Service
public class UserService {

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (MarketUserDetails) ctx.getAuthentication().getPrincipal())
                .map(MarketUserDetails::getUserId);
    }
}