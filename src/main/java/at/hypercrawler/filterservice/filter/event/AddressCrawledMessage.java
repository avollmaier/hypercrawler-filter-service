package at.hypercrawler.filterservice.filter.event;

import java.util.List;
import java.util.UUID;

public record AddressCrawledMessage(UUID crawlerId, List<String> rawAddresses) {
}
