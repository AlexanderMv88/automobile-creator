package org.automobilecreator;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AcceptanceTest {

    @Value(value = "${local.server.port}")
    private int port;

    private static final Logger log = LoggerFactory.getLogger(AcceptanceTest.class);

    public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
            .parse("mockserver/mockserver")
            .withTag("mockserver-5.15.0");

    @Rule
    public static MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        mockServer.start();
        log.info("engine.service.port: "+mockServer.getServerPort());
        registry.add("engine.service.port", () -> mockServer.getServerPort());
        String engineHost = mockServer.getContainerInfo().getNetworkSettings().getNetworks().get("bridge").getGateway();
        log.info("engine.service.host: "+engineHost);
        registry.add("engine.service.host", () -> String.format(engineHost));
    }


    @Test
    public void shouldCreateAllPartsAndBuildAutomobile(){
        ObjectMapper mapper = new ObjectMapper();
        try (
                MockServerClient mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort())
        ) {
            Boolean isTurbo = false;
            CarEngine engine = new CarEngine("EngineName", new BigDecimal("5.2"), isTurbo);
            Integer donePercent = 100;
            CarEngineInfo engineInfo = new CarEngineInfo(engine, donePercent);
            String engineJsonString = mapper.writeValueAsString(engine);
            String engineJsonResponseString = mapper.writeValueAsString(engineInfo);
            mockServerClient
                    .when(request().withPath("/api/v1/create").withMethod("post").withBody(engineJsonString))
                    .respond(response().withBody(engineJsonResponseString)
                            .withHeader("Content-type", "application/json").withHeader("charset", "utf-8")
                    );

            System.out.println("AAA");
            assertThat("AAA").isEqualTo("BBB");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
