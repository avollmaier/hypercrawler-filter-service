package at.hypercrawler.filterservice.event;

import at.hypercrawler.filterservice.domain.service.FilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
public class FilterFunctions {

    private final FilterService filterService;

    public FilterFunctions(FilterService filterService) {
        this.filterService = filterService;
    }

    @Bean
    public Function<Flux<AddressCrawledMessage>, Mono<Void>> filter() {
        return addressSupplyMessageFlux -> addressSupplyMessageFlux.mapNotNull(addressCrawledMessage -> {
            log.info("Filtering address {}", addressCrawledMessage.rawAddress());
            filterService.filter(addressCrawledMessage.rawAddress(), addressCrawledMessage.crawlerId());

            return Mono.empty();
        }).then();
    }

}
