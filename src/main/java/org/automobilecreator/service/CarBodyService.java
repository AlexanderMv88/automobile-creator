package org.automobilecreator.service;

import org.automobilecreator.dto.CarBody;
import org.automobilecreator.dto.CarBodyInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CarBodyService {

    private final WebClient bodyClient;

    public CarBodyService(WebClient bodyClient) {
        this.bodyClient = bodyClient;
    }

    public Mono<CarBodyInfo> getCarBodyInfoMono(CarBody body) {
        Assert.notNull(body, "Кузов null!!!");
        return bodyClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(body), CarBody.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarBodyInfo.class)
                .timeout(Duration.ofMillis(2000));
    }
}
