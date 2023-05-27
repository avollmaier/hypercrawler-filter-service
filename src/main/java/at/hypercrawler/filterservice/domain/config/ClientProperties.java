package at.hypercrawler.filterservice.domain.config;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Value
@ConfigurationProperties(prefix = "hypercrawler.filter-service.client")
public class ClientProperties {
    @NotNull
    URI managerServiceUri;
}
