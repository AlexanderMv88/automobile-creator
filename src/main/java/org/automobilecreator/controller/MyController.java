package org.automobilecreator.controller;

import org.automobilecreator.CarWheels;
import org.automobilecreator.dto.*;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MyController {

    public MyController(WebClient engineClient, WebClient bodyClient, WebClient wheelClient) {
        this.engineClient = engineClient;
        this.bodyClient = bodyClient;
        this.wheelClient = wheelClient;
    }

    private final WebClient engineClient;
    private final WebClient bodyClient;
    private final WebClient wheelClient;
    @PostMapping("/build")
    public Mono<CreationResult> build(@RequestBody Parts parts) {
        CarEngine engine = parts.engine();
        Assert.notNull(engine, "Двигатель null!!!");

        Mono<CarEngineInfo> carEngineInfoMono = engineClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(engine), CarEngine.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarEngineInfo.class)
                .timeout(Duration.ofMillis(2000));


        CarBody body = parts.body();
        Assert.notNull(body, "Кузов null!!!");
        Mono<CarBodyInfo> carBodyInfoMono = bodyClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(body), CarBody.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarBodyInfo.class)
                .timeout(Duration.ofMillis(2000));

        CarWheels carWheels = parts.wheels();

        Flux<CarWheelInfo> carWheelInfoFlux = Flux.fromIterable(carWheels.wheels()).flatMap(this::getCarWheelInfoMono);

        Mono<Tuple3<CarEngineInfo, CarBodyInfo, List<CarWheelInfo>>> zip = Mono.zip(carEngineInfoMono, carBodyInfoMono, carWheelInfoFlux.collectList());
        Mono<CreationResult> creationResultMono = zip.map(it -> new CreationResult(it.getT1(), it.getT2(), it.getT3()));
        return creationResultMono;
    }

    private Mono<CarWheelInfo> getCarWheelInfoMono(CarWheel wheel) {
        Assert.notNull(wheel, "Колесо null!!!");
        Mono<CarWheelInfo> carWheelsInfoMono = wheelClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(wheel), CarWheel.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarWheelInfo.class)
                .timeout(Duration.ofMillis(2000));
        return carWheelsInfoMono;
    }

}
