package at.hypercrawler.filterservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CrawlerResponse(UUID id, String name, CrawlerStatus status, CrawlerConfig config, Instant createdAt,
                              Instant updatedAt) {
}
