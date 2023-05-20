package at.hypercrawler.filterservice.domain.service;

import at.hypercrawler.filterservice.domain.model.CrawlerResponse;
import at.hypercrawler.filterservice.domain.model.SupportedFileType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class FilterService {
    public Optional<URL> filter(String adress, UUID crawlerId) {
        try {
            String extension = adress.substring(adress.lastIndexOf("."));
            List<SupportedFileType> supportedFileTypes = getSupportedFileTypes(crawlerId);

            if (supportedFileTypes.stream().anyMatch(supportedFileType -> supportedFileType.getFormat().equals(extension))) {
                return Optional.of(new URL(adress));
            }
            return Optional.empty();

        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    private List<SupportedFileType> getSupportedFileTypes(UUID crawlerId) {
        WebClient webClient = WebClient.create("http://localhost:9003");
        return Objects.requireNonNull(webClient.get()
                .uri("/" + crawlerId)
                .retrieve()
                .bodyToMono(CrawlerResponse.class)
                .block())
                .config()
                .fileTypesToMatch();
    }
}
