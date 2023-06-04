package at.hypercrawler.filterservice.manager;

import at.hypercrawler.filterservice.config.ClientProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.Random.class)
class ManagerClientTest {

    private MockWebServer mockWebServer;
    private ManagerClient managerClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.managerClient = new ManagerClient(new ClientProperties(mockWebServer.url("/").uri()), webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void whenCrawlerExists_thenReturnStatus() {
        UUID crawlerId = UUID.randomUUID();

        MockResponse mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                            {
                               "indexPrefix":"crawler_",
                               "schedule":"0 0 0 1 1 ? 2099",
                               "startUrls":[
                                  "https://www.google.com",
                                  "https://www.bing.com"
                               ],
                               "filterOptions":{
                                  "siteExclusionPatterns":[
                                     "https://www.google.com/**"
                                  ],
                                  "queryParameterExclusionPatterns":[
                                     "utm_*"
                                  ]
                               },
                               "requestOptions":{
                                  "proxy":{
                                     "host":"localhost",
                                     "port":8080
                                  },
                                  "requestTimeout":1000,
                                  "retries":3,
                                  "headers":[
                                     {
                                        "name":"User-Agent",
                                        "value":"Mozilla/5.0 (compatible"
                                     }
                                  ]
                               },
                               "robotOptions":{
                                  "ignoreRobotRules":true,
                                  "ignoreRobotNoIndex":true,
                                  "ignoreRobotNoFollowTo":true
                               },
                               "actions":[
                                  {
                                     "indexName":"test_index",
                                     "pathsToMatch":[
                                        "http://www.foufos.gr/*"
                                     ],
                                     "selectorsToMatch":[
                                        ".products",
                                        "!.featured"
                                     ],
                                     "contentTypesToMatch":[
                                        "HTML",
                                        "PDF"
                                     ]
                                  }
                               ]
                            }
                        """);

        mockWebServer.enqueue(mockResponse);

        Mono<CrawlerConfig> crawlerConfigMono = managerClient.getCrawlerConfigById(crawlerId);

        StepVerifier.create(crawlerConfigMono)
                .expectNextMatches(s -> s.equals(CrawlerTestDummyProvider.crawlerConfig.get()))
                .verifyComplete();
    }

    @Test
    void whenCrawlerNotExists_thenReturnEmpty() {
        UUID crawlerId = UUID.randomUUID();

        MockResponse mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404);

        mockWebServer.enqueue(mockResponse);

        StepVerifier.create(managerClient.getCrawlerConfigById(crawlerId))
                .expectNextCount(0)
                .verifyComplete();
    }

}