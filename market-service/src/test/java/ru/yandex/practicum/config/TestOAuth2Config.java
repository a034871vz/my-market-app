package ru.yandex.practicum.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@TestConfiguration
public class TestOAuth2Config {

    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository() {
        return new InMemoryReactiveClientRegistrationRepository(
                org.springframework.security.oauth2.client.registration.ClientRegistration
                        .withRegistrationId("payment-service")
                        .clientId("test-client")
                        .clientSecret("test-secret")
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .tokenUri("http://localhost:8082/token")
                        .scope("payment")
                        .build()
        );
    }
}