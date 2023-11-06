package org.automobilecreator.service;

import org.automobilecreator.dto.CarWheel;
import org.automobilecreator.dto.CarWheelInfo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CarWheelService {


    private final WebClient wheelClient;

    public CarWheelService(WebClient wheelClient) {
        this.wheelClient = wheelClient;
    }

    public Mono<CarWheelInfo> getCarWheelInfoMono(CarWheel wheel) {
        Assert.notNull(wheel, "Колесо null!!!");
        return wheelClient.post()
                .uri("/api/v1/create")
                .body(Mono.just(wheel), CarWheel.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CarWheelInfo.class)
                .timeout(Duration.ofMillis(2000));
    }
}
