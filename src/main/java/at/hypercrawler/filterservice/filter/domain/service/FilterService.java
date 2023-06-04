package at.hypercrawler.filterservice.filter.domain.service;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import at.hypercrawler.filterservice.filter.domain.util.RegexUtil;
import at.hypercrawler.filterservice.filter.event.AddressCrawledMessage;
import at.hypercrawler.filterservice.filter.event.AddressFilteredMessage;
import at.hypercrawler.filterservice.manager.CrawlerFilterOptions;
import at.hypercrawler.filterservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FilterService {

    public static final String FILTER_ADDRESS_OUT = "filter-out-0";

    private final StreamBridge streamBridge;

    private final ManagerClient managerClient;

    private final ExtendedURLNormalizer extendedURLNormalizer;

    public FilterService(StreamBridge streamBridge, ManagerClient managerClient, ExtendedURLNormalizer extendedURLNormalizer) {
        this.streamBridge = streamBridge;
        this.managerClient = managerClient;
        this.extendedURLNormalizer = extendedURLNormalizer;
    }

    private void publishAddressFilteredEvent(List<URL> address, UUID crawlerId) {
        AddressFilteredMessage addressSupplyMessage = new AddressFilteredMessage(crawlerId, address);
        boolean result = streamBridge.send(FILTER_ADDRESS_OUT, addressSupplyMessage);
        log.info("Result of sending address {} for crawler with id: {} is {}", address, crawlerId, result);
    }

    public Flux<AddressFilteredMessage> consumeAddressCrawledEvent(Flux<AddressCrawledMessage> flux) {
        log.info("Consuming address prioritized event");

        return flux.flatMap(e -> managerClient.getCrawlerConfigById(e.crawlerId())
                .doOnNext(config -> log.info("Crawler config for crawler {} is {}", e.crawlerId(), config))
                .flatMap(config ->
                        filter(config.filterOptions(), e.rawAddresses(), e.crawlerId())
                                .map(filteredUrl -> new AddressFilteredMessage(e.crawlerId(), filteredUrl))));
    }


    private Mono<List<URL>> filter(CrawlerFilterOptions filterOptions, List<String> rawAddresses, UUID crawlerId) {
        log.info("Filtering unfiltered address {} for crawler {}", rawAddresses.size(), crawlerId);
        List<URL> normalizedAddress = extendedURLNormalizer.apply(rawAddresses);

        if (filterOptions == null) {
            log.info("No filter options found, returning unfiltered addresses");
            return Mono.just(normalizedAddress);
        }

        Pattern siteExclusionPatterns = RegexUtil.combineRegex(filterOptions.siteExclusionPatterns());
        Pattern queryParameterExclusionPatterns = RegexUtil.combineRegex(filterOptions.queryParameterExclusionPatterns());

        List<URL> queryParameterFilteredOut = normalizedAddress.stream()
                .filter(url -> url.getQuery() == null)
                .filter(url -> queryParameterExclusionPatterns.matcher(url.getHost()).matches())
                .toList();

        log.info("Filtered out {} addresses with the query parameter exclusion filter", queryParameterFilteredOut.size());

        List<URL> siteFilteredOut = normalizedAddress.stream()
                .filter(url -> siteExclusionPatterns.matcher(url.getHost()).matches())
                .toList();

        log.info("Filtered out {} addresses with the site exclusion filter", siteFilteredOut.size());

        normalizedAddress = normalizedAddress.stream().filter(url -> !siteFilteredOut.contains(url)).toList();
        normalizedAddress = normalizedAddress.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return Mono.just(normalizedAddress).doOnNext(addresses -> publishAddressFilteredEvent(addresses, crawlerId));

    }

}
