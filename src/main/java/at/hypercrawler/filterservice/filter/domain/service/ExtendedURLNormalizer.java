package at.hypercrawler.filterservice.filter.domain.service;

import crawlercommons.filters.basic.BasicURLNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Component
public class ExtendedURLNormalizer implements Function<List<String>, List<URL>> {
    @Override
    public List<URL> apply(List<String> rawAddresses) {

        return rawAddresses.stream()
                .filter(Objects::nonNull)
                .map(url -> new BasicURLNormalizer().filter(url))
                .filter(Objects::nonNull)
                .map(this::parseUrl)
                .filter(Objects::nonNull)
                .toList();
    }

    private URL parseUrl(String address) {
        try {
            return new URL(address);
        } catch (Exception e) {
            log.warn("Could not parse address {}", address);
            return null;
        }
    }
}
