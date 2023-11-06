package org.automobilecreator.controller;

import org.automobilecreator.service.CarBodyService;
import org.automobilecreator.service.CarEngineService;
import org.automobilecreator.dto.CarWheels;
import org.automobilecreator.service.CarWheelService;
import org.automobilecreator.dto.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MyController {

    public MyController(CarEngineService carEngineService, CarBodyService carBodyService, CarWheelService carWheelService) {
        this.carEngineService = carEngineService;
        this.carBodyService = carBodyService;
        this.carWheelService = carWheelService;
    }

    private final CarEngineService carEngineService;
    private final CarBodyService carBodyService;
    private final CarWheelService carWheelService;

    @PostMapping("/build")
    public Mono<CreationResult> build(@RequestBody Parts parts) {

        Mono<CarEngineInfo> carEngineInfoMono = carEngineService.getCarEngineInfoMono(parts.engine());

        Mono<CarBodyInfo> carBodyInfoMono = carBodyService.getCarBodyInfoMono(parts.body());

        CarWheels carWheels = parts.wheels();
        Flux<CarWheelInfo> carWheelInfoFlux = Flux.fromIterable(carWheels.wheels()).flatMap(carWheelService::getCarWheelInfoMono);
        Mono<Tuple3<CarEngineInfo, CarBodyInfo, List<CarWheelInfo>>> zip =
                Mono.zip(carEngineInfoMono, carBodyInfoMono, carWheelInfoFlux.collectList());

        return zip.map(it -> new CreationResult(it.getT1(), it.getT2(), it.getT3()));
    }




}
