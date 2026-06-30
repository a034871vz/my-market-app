package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.payment.client.PaymentApi;
import ru.yandex.practicum.payment.invoker.ApiClient;

@Configuration
public class PaymentClientConfig {

    public static final String PAYMENT_CLIENT_REGISTRATION_ID = "payment-service";

    @Bean
    public PaymentApi paymentApi(ReactiveOAuth2AuthorizedClientManager authorizedClientManager, @Value("${payment.service.url}") String baseUrl) {

        var oauth = new org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(PAYMENT_CLIENT_REGISTRATION_ID);

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(oauth)
                .filter((request, next) -> {
                    System.out.println("Request headers: " + request.headers());
                    return next.exchange(request);
                })
                .build();

        ApiClient apiClient = new ApiClient(webClient);

        PaymentApi paymentApi = new PaymentApi();
        paymentApi.setApiClient(apiClient);

        return paymentApi;
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                         ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(configurer -> {
                    WebClientReactiveClientCredentialsTokenResponseClient clientCredentialsTokenResponseClient =
                            new WebClientReactiveClientCredentialsTokenResponseClient();
                    configurer.accessTokenResponseClient(clientCredentialsTokenResponseClient);
                })
                .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
