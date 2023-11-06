package org.automobilecreator.service;

import org.automobilecreator.dto.CarEngine;
import org.automobilecreator.dto.CarEngineInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CarEngineService {

    private final WebClient engineClient;

    public CarEngineService(WebClient engineClient) {
        this.engineClient = engineClient;
    }

    public Mono<CarEngineInfo> getCarEngineInfoMono(CarEngine engine) {
        Assert.notNull(engine, "Двигатель null!!!");
        return engineClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(engine), CarEngine.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarEngineInfo.class)
                .timeout(Duration.ofMillis(2000));
    }
}
