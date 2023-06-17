package at.hypercrawler.filterservice.filter.event;

import at.hypercrawler.filterservice.manager.CrawlerTestDummyProvider;
import at.hypercrawler.filterservice.manager.ManagerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@FunctionalSpringBootTest
@AutoConfiguration
class FilterMessageTest {

  @MockBean
  ManagerClient managerClient;
  @Autowired
  private FunctionCatalog catalog;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private OutputDestination output;

  @BeforeEach
  void setUp() {
  }

  @Test
  void whenFilterWithExclusion_CorrectUrlsAreSend()
          throws IOException {

    URL address = new URL("http://www.google.com/");
    URL address2 = new URL("http://www.google.at/");
    URL address3 = new URL("http://www.goooogle.de/");
    UUID uuid = UUID.randomUUID();

    when(managerClient.getCrawlerConfigById(uuid)).then(
            invocation -> Mono.just(CrawlerTestDummyProvider.crawlerConfig.get()));

    Function<Flux<AddressCrawledMessage>, Flux<AddressSuppliedMessage>> filter =

            catalog.lookup(Function.class, "filter");

    Flux<AddressCrawledMessage> addressCrawledMessageFlux =
            Flux.just(new AddressCrawledMessage(uuid, List.of(address.toString(), address2.toString(), address3.toString())));
    StepVerifier.create(filter.apply(addressCrawledMessageFlux)).expectNextCount(0).verifyComplete();

    assertThat(objectMapper.readValue(output.receive().getPayload(), AddressSuppliedMessage.class)).isEqualTo(
            new AddressSuppliedMessage(uuid, List.of(address3)));

  }

  @Test
  void whenFilterWithQueryParameterExclusion_CorrectUrlsAreSend()
          throws IOException {

    URL address = new URL("http://www.goooogle.com/?utm_source=google");
    URL address2 = new URL("http://www.goooogle.at/?q=2");
    URL address3 = new URL("http://www.goooogle.de/?q=3");
    UUID uuid = UUID.randomUUID();

    when(managerClient.getCrawlerConfigById(uuid)).then(
            invocation -> Mono.just(CrawlerTestDummyProvider.crawlerConfig.get()));

    Function<Flux<AddressCrawledMessage>, Flux<AddressSuppliedMessage>> filter =

            catalog.lookup(Function.class, "filter");

    Flux<AddressCrawledMessage> addressCrawledMessageFlux =
            Flux.just(new AddressCrawledMessage(uuid, List.of(address.toString(), address2.toString(), address3.toString())));
    StepVerifier.create(filter.apply(addressCrawledMessageFlux)).expectNextCount(0).verifyComplete();

    assertThat(objectMapper.readValue(output.receive().getPayload(), AddressSuppliedMessage.class)).isEqualTo(
            new AddressSuppliedMessage(uuid, List.of(address2, address3)));

  }

  @Test
  void whenFilterWithStoppedCrawler_thenNoMessageSend()
          throws IOException {

    URL address = new URL("http://www.google.com/");
    URL address2 = new URL("http://www.google.at/");
    UUID uuid = UUID.randomUUID();

    when(managerClient.getCrawlerConfigById(uuid)).then(invocation -> Mono.empty());

    Function<Flux<AddressCrawledMessage>, Flux<AddressSuppliedMessage>> filter =

            catalog.lookup(Function.class, "filter");

    Flux<AddressCrawledMessage> addressCrawledMessageFlux =
      Flux.just(new AddressCrawledMessage(uuid, List.of(address.toString(), address2.toString())));
    StepVerifier.create(filter.apply(addressCrawledMessageFlux)).expectNextCount(0).verifyComplete();

    assertNull(output.receive());

  }

}
