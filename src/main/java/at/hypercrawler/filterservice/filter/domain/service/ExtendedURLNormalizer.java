package at.hypercrawler.filterservice.filter.domain.service;

import at.hypercrawler.filterservice.filter.domain.util.UrlParser;
import crawlercommons.filters.basic.BasicURLNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Component
public class ExtendedURLNormalizer implements Function<List<String>, List<URL>> {
    @Override
    public List<URL> apply(List<String> rawAddresses) {

        if (rawAddresses == null || rawAddresses.isEmpty()) {
            return Collections.emptyList();
        }
        return rawAddresses.stream().filter(Objects::nonNull).map(url -> new BasicURLNormalizer().filter(url))
          .filter(Objects::nonNull).map(UrlParser::parseUrl).filter(Objects::nonNull).toList();
    }

}
