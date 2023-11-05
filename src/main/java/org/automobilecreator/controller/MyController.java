package org.automobilecreator.controller;

import org.automobilecreator.dto.*;
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

    public MyController(WebClient engineClient, WebClient bodyClient) {
        this.engineClient = engineClient;
        this.bodyClient = bodyClient;
    }

    private final WebClient engineClient;
    private final WebClient bodyClient;
    @PostMapping("/build")
    public Mono<CreationResult> build(@RequestBody Parts parts) {
        //TODO: реализовать походы за двигателем и кузовом одновременно (двигатель и кузов) (OK)
        //TODO: реализовать походы за всеми колесами сразу
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


        CarBody body = parts.body();
        Assert.notNull(body, "Кузов null!!!");
        Mono<CarBodyInfo> carBodyInfoMono = bodyClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(body), CarBody.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarBodyInfo.class)
                .timeout(Duration.ofMillis(1000));

        Mono<CreationResult> creationResultMono = Mono.zip(carEngineInfoMono, carBodyInfoMono, CreationResult::new);
        return creationResultMono;
    }

}
