package at.hypercrawler.filterservice.filter.event;

import at.hypercrawler.filterservice.filter.domain.service.FilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class FilterFunctions {

    private final FilterService filterService;

    public FilterFunctions(FilterService filterService) {
        this.filterService = filterService;
    }

    @Bean
    public Consumer<Flux<AddressCrawledMessage>> filter() {
        log.info("Creating filter function");
        return flux -> filterService.consumeAddressCrawledEvent(flux)
                .doOnNext(e -> log.info("Consuming address crawled event for the crawler with id: {}", e.crawlerId()))
                .subscribe();
    }

}
