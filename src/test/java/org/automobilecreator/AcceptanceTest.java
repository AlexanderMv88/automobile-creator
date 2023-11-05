package org.automobilecreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automobilecreator.dto.*;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

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
    public static MockServerContainer mockEngineService = new MockServerContainer(MOCKSERVER_IMAGE);
    @Rule
    public static MockServerContainer mockBodyService = new MockServerContainer(MOCKSERVER_IMAGE);

    @DynamicPropertySource
    static void mockEngineServiceProperties(DynamicPropertyRegistry registry) {
        buildMockServiceProperties(registry, "engine", mockEngineService);
    }

    @DynamicPropertySource
    static void mockBodyServiceProperties(DynamicPropertyRegistry registry) {
        buildMockServiceProperties(registry, "body", mockBodyService);
    }

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateAllPartsAndBuildAutomobile() throws com.fasterxml.jackson.core.JsonProcessingException {
        try {
            //TODO: правильно остановить
            MockServerClient mockEngineServerClient = new MockServerClient(mockEngineService.getHost(), mockEngineService.getServerPort());
            MockServerClient mockBodyServerClient = new MockServerClient(mockBodyService.getHost(), mockBodyService.getServerPort());
            //given
            CarEngine engine = new CarEngine("EngineName",
                    new BigDecimal("5.2"),
                    false);
            //TODO: Сделать универсальную обертку
            CarEngineInfo engineInfo = new CarEngineInfo(engine, 100);

            prepareEngineMockServer(mockEngineServerClient, engine, engineInfo);

            CarBody body = new CarBody("Универсал");
            CarBodyInfo bodyInfo = new CarBodyInfo(body, 100);
            prepareBodyMockServer(mockBodyServerClient, body, bodyInfo);

            Parts parts = new Parts(engine, body);

            //when
            CreationResult responseBody = webClient.post()
                    .uri("/api/v1/build")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(parts)
                    .exchange()
            //then
                    .expectStatus().isOk()
                    .expectBody(CreationResult.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(responseBody).isEqualTo(new CreationResult(engineInfo, bodyInfo));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }




    private static void buildMockServiceProperties(DynamicPropertyRegistry registry, String serviceName, MockServerContainer container) {
        container.start();
        log.info(serviceName+".service.port: "+ container.getServerPort());
        registry.add(serviceName+".service.port", () -> container.getServerPort());
        String engineHost = container.getContainerInfo().getNetworkSettings().getNetworks().get("bridge").getGateway();
        log.info(serviceName+".service.host: "+engineHost);
        registry.add(serviceName+".service.host", () -> String.format(engineHost));
    }

    private void prepareEngineMockServer(MockServerClient mockServerClient, CarEngine engine, CarEngineInfo engineInfo) throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        String engineJsonString = objectMapper.writeValueAsString(engine);
        String engineJsonResponseString = objectMapper.writeValueAsString(engineInfo);
        mockServerClient
                .when(request()
                        .withPath("/api/v1/create")
                        .withMethod("post")
                        .withBody(engineJsonString)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                )
                .respond(response().withBody(engineJsonResponseString)
                        .withHeader("Content-type", "application/json").withHeader("charset", "utf-8")
                );
    }

    private void prepareBodyMockServer(MockServerClient mockServerClient, CarBody body, CarBodyInfo bodyInfo) throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        String bodyJsonString = objectMapper.writeValueAsString(body);
        String bodyJsonResponseString = objectMapper.writeValueAsString(bodyInfo);
        mockServerClient
                .when(request()
                        .withPath("/api/v1/create")
                        .withMethod("post")
                        .withBody(bodyJsonString)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                )
                .respond(response().withBody(bodyJsonResponseString)
                        .withHeader("Content-type", "application/json").withHeader("charset", "utf-8")
                );
    }
}
