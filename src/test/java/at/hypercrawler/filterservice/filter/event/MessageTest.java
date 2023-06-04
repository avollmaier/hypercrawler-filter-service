package at.hypercrawler.filterservice.filter.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.hypercrawler.filterservice.manager.CrawlerTestDummyProvider;
import at.hypercrawler.filterservice.manager.ManagerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@FunctionalSpringBootTest
@AutoConfiguration
class MessageTest {

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
  void whenFilterWithRunningCrawler_thenMessageSend()
    throws IOException {

    URL address = new URL("http://www.google.com/");
    URL address2 = new URL("http://www.google.at/");
    UUID uuid = UUID.randomUUID();

    when(managerClient.getCrawlerConfigById(uuid)).then(
      invocation -> Mono.just(CrawlerTestDummyProvider.crawlerConfig.get()));

    Function<Flux<AddressCrawledMessage>, Flux<Message<AddressFilteredMessage>>> filter =

      catalog.lookup(Function.class, "filter");

    Flux<AddressCrawledMessage> addressCrawledMessageFlux =
      Flux.just(new AddressCrawledMessage(uuid, List.of(address.toString(), address2.toString())));
    StepVerifier.create(filter.apply(addressCrawledMessageFlux)).expectNextCount(0).verifyComplete();

    assertThat(objectMapper.readValue(output.receive().getPayload(), AddressFilteredMessage.class)).isEqualTo(
      new AddressFilteredMessage(uuid, List.of(address, address2)));

  }

  @Test
  void whenFilterWithStoppedCrawler_thenMessageSend()
    throws IOException {

    URL address = new URL("http://www.google.com/");
    URL address2 = new URL("http://www.google.at/");
    UUID uuid = UUID.randomUUID();

    when(managerClient.getCrawlerConfigById(uuid)).then(invocation -> Mono.empty());

    Function<Flux<AddressCrawledMessage>, Flux<Message<AddressFilteredMessage>>> filter =

      catalog.lookup(Function.class, "filter");

    Flux<AddressCrawledMessage> addressCrawledMessageFlux =
      Flux.just(new AddressCrawledMessage(uuid, List.of(address.toString(), address2.toString())));
    StepVerifier.create(filter.apply(addressCrawledMessageFlux)).expectNextCount(0).verifyComplete();

    assertNull(output.receive());

  }

}
