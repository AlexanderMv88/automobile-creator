package org.automobilecreator;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class MyController {
    @PostMapping("/build")
    public CreationResult build(@RequestBody Parts parts) {
        //TODO: ходить с помощью webClient за двигателем
        //TODO: + ходить в другой сервис за кузовом
        //TODO: реализовать походы за двигателем и кузовом с помощью concurrency (двигатель и кузов это Part)
        //TODO: + ходить в другой сервис за колесами (Part)
        CarEngine engine = new CarEngine("EngineName", new BigDecimal("5.2"), false);
        return new CreationResult(new CarEngineInfo(engine, 100));
    }

}
