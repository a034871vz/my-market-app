package ru.yandex.practicum.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.config.MarketUserDetails;

@Service
public class UserService {

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof MarketUserDetails) {
                        return Mono.just(((MarketUserDetails) auth.getPrincipal()).getUserId());
                    }
                    return Mono.empty();
                })
                .onErrorReturn(-1L);
    }
}