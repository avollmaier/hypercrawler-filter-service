package at.hypercrawler.filterservice.filter.domain.service;

import at.hypercrawler.filterservice.filter.domain.util.RegexUtil;
import at.hypercrawler.filterservice.filter.event.AddressCrawledMessage;
import at.hypercrawler.filterservice.filter.event.AddressSuppliedMessage;
import at.hypercrawler.filterservice.manager.CrawlerFilterOptions;
import at.hypercrawler.filterservice.manager.ManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @NotNull
    private static List<URL> filterOutBySiteExclusion(List<URL> normalizedAddress, Pattern siteExclusionPatterns) {
        return normalizedAddress.stream()
                .filter(url -> url.getHost() != null)
                .peek(url -> log.debug("Checking if url {} matches the site exclusion pattern {}", url, siteExclusionPatterns))
                .filter(url -> siteExclusionPatterns.matcher(url.getHost()).matches())
                .toList();
    }

    @NotNull
    private static List<URL> filterOutByQueryParameter(List<URL> normalizedAddress, Pattern queryParameterExclusionPatterns) {
        return normalizedAddress.stream()
                .filter(url -> url.getQuery() != null)
                .peek(url -> log.info("Checking if url query {} matches the query parameter exclusion pattern {}", url.getQuery(), queryParameterExclusionPatterns))
                .filter(url -> queryParameterExclusionPatterns.matcher(url.getQuery()).matches())
                .toList();
    }

    private void publishAddressFilteredEvent(List<URL> address, UUID crawlerId) {
        AddressSuppliedMessage addressSupplyMessage = new AddressSuppliedMessage(crawlerId, address);
        boolean result = streamBridge.send(FILTER_ADDRESS_OUT, addressSupplyMessage);
        log.info("Result of sending address {} for crawler with id: {} is {}", address, crawlerId, result);
    }

    public Flux<AddressSuppliedMessage> consumeAddressCrawledEvent(Flux<AddressCrawledMessage> flux) {
        log.info("Consuming address prioritized event");

        return flux.flatMap(e -> managerClient.getCrawlerConfigById(e.crawlerId())
                .doOnNext(config -> log.info("Crawler config for crawler {} is {}", e.crawlerId(), config))
                .flatMap(config ->
                        filter(config.filterOptions(), e.rawAddresses(), e.crawlerId())
                                .map(filteredUrl -> new AddressSuppliedMessage(e.crawlerId(), filteredUrl))));
    }

    private Mono<List<URL>> filter(CrawlerFilterOptions filterOptions, List<String> rawAddresses, UUID crawlerId) {
        log.info("Filtering unfiltered address {} for crawler {}", rawAddresses.size(), crawlerId);
        List<URL> normalizedAddress = extendedURLNormalizer.apply(rawAddresses);

        if (filterOptions == null) {
            log.warn("No filter options found, returning unfiltered addresses");
            return Mono.just(normalizedAddress);
        }
        Pattern siteExclusionPatterns = RegexUtil.combineRegex(filterOptions.siteExclusionPatterns());
        Pattern queryParameterExclusionPatterns = RegexUtil.combineRegex(filterOptions.queryParameterExclusionPatterns());

        List<URL> queryParameterFilteredOut = filterOutByQueryParameter(normalizedAddress, queryParameterExclusionPatterns);
        log.info("Filtered out {} addresses with the query parameter exclusion filter", queryParameterFilteredOut.size());

        List<URL> siteFilteredOut = filterOutBySiteExclusion(normalizedAddress, siteExclusionPatterns);
        log.info("Filtered out {} addresses with the site exclusion filter", siteFilteredOut.size());

        normalizedAddress = normalizedAddress.stream().filter(url -> !siteFilteredOut.contains(url)).filter(url -> !queryParameterFilteredOut.contains(url)).toList();
        normalizedAddress = normalizedAddress.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return Mono.just(normalizedAddress).doOnNext(addresses -> publishAddressFilteredEvent(addresses, crawlerId));
    }

}
