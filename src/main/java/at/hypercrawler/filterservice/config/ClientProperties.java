package at.hypercrawler.filterservice.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "hypercrawler.filter-service.client")
public record ClientProperties(

        @NotNull
        URI managerServiceUri

) {
}
