package org.automobilecreator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Value("${engine.service.port}")
    private String engineServicePort;

    @Value("${engine.service.host}")
    private String engineServiceHost;

    Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient engineClient(WebClient.Builder webClientBuilder){
        return webClientBuilder.baseUrl("http://" + engineServiceHost + ":" + engineServicePort)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest());
                })
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n");
                log.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }


}
