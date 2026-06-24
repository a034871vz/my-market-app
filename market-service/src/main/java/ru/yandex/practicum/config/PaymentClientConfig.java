package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.payment.client.PaymentApi;
import ru.yandex.practicum.payment.invoker.ApiClient;

@Configuration
public class PaymentClientConfig {

    @Bean
    public PaymentApi paymentApi(@Value("${payment.service.url}") String baseUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);

        PaymentApi paymentApi = new PaymentApi();
        paymentApi.setApiClient(apiClient);

        return paymentApi;
    }
}
