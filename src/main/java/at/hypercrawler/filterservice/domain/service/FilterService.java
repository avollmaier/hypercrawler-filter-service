package at.hypercrawler.filterservice.domain.service;

import at.hypercrawler.filterservice.domain.config.ClientProperties;
import at.hypercrawler.filterservice.domain.util.GlobConverter;
import at.hypercrawler.filterservice.event.AddressSuppliedMessage;
import at.hypercrawler.managerservice.dto.CrawlerAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FilterService {

    private final WebClient webClient;

    private final StreamBridge streamBridge;
    private final ClientProperties clientProperties;

    private final GlobConverter globConverter;

    public FilterService(WebClient webClient, StreamBridge streamBridge, ClientProperties clientProperties, GlobConverter globConverter) {
        this.webClient = webClient;
        this.streamBridge = streamBridge;
        this.clientProperties = clientProperties;
        this.globConverter = globConverter;
    }

    public void filter(String address, UUID crawlerId) {
        CrawlerAction crawlerAction = getCrawlerAction(crawlerId);
        String regexPathsToMatch = globConverter.convertGlobsToRegex(crawlerAction.getPathsToMatch());

        if (!address.matches(regexPathsToMatch)) {
            log.debug("Address {} does not match the regex {}", address, regexPathsToMatch);
            return;
        }


        try {
            URL url = new URL(address);
            publishAddressCrawledEvent(url, crawlerId);
        } catch (MalformedURLException e) {
            log.warn("Malformed URL: {}", address);
        }
    }

    private void publishAddressCrawledEvent(URL address, UUID crawlerId) {
        AddressSuppliedMessage addressSupplyMessage = new AddressSuppliedMessage(crawlerId, address);
        streamBridge.send("filter-out-0", addressSupplyMessage);
    }

    private CrawlerAction getCrawlerAction(UUID crawlerId) {
        return Objects.requireNonNull(webClient.get()
                .uri(clientProperties.getManagerServiceUri() + "/crawlers/" + crawlerId + "/action")
                .retrieve()
                .bodyToMono(CrawlerAction.class)
                .block());
    }
}
