package at.hypercrawler.filterservice.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FilterConfig {

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }
}
