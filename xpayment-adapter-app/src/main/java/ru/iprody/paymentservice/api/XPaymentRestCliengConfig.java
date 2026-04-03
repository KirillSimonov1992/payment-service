package ru.iprody.paymentservice.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.iprody.xpayment.api.ApiClient;
import ru.iprody.xpayment.api.client.DefaultApi;

@Configuration
class XPaymentRestCliengConfig {

    @Bean
    RestTemplate xpaymentRestTemplate(
            @Value("${app.x-payment-api.client.username") String username,
            @Value("${app.x-payment-api.cient.password") String password,
            @Value("${app.x-payment-api.cient.account") String xPayAccount
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
                .add(
                        (request, body, execution) -> {
                            request.getHeaders().setBasicAuth(username, password);
                            request.getHeaders().set("X-Pay-Account", xPayAccount);
                            return execution.execute(request, body);
                        }
                );
        return restTemplate;
    }

    @Bean
    ApiClient xpaymentApiClient(
            @Value("${app.x-payment-api.client.url") String xPaymentApiUrl,
            RestTemplate xpaymentRestTemplate
    ) {
        ApiClient apiClient = new ApiClient(xpaymentRestTemplate);
        apiClient.setBasePath(xPaymentApiUrl);
        return apiClient;
    }

    @Bean
    DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}