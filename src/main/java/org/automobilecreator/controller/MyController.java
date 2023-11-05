package org.automobilecreator.controller;

import org.automobilecreator.dto.CarEngine;
import org.automobilecreator.dto.CarEngineInfo;
import org.automobilecreator.dto.CreationResult;
import org.automobilecreator.dto.Parts;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class MyController {

    public MyController(WebClient engineClient) {
        this.engineClient = engineClient;
    }

    private final WebClient engineClient;
    @PostMapping("/build")
    public Mono<CreationResult> build(@RequestBody Parts parts) {
        //TODO: + ходить в другой сервис за кузовом
        //TODO: реализовать походы за двигателем и кузовом с помощью concurrency (двигатель и кузов это Part)
        //TODO: + ходить в другой сервис за колесами (Part)
        CarEngine engine = parts.engine();
        Assert.notNull(engine, "Двигатель null!!!");

        Mono<CarEngineInfo> carEngineInfoMono = engineClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(engine), CarEngine.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarEngineInfo.class)
                .timeout(Duration.ofMillis(1000));

        return carEngineInfoMono.map(carEngineInfo -> {
            System.out.println("carEngineInfo = " + carEngineInfo);
            return new CreationResult(carEngineInfo);
        });
    }

}
