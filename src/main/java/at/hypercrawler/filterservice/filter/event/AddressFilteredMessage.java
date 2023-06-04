package at.hypercrawler.filterservice.filter.event;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public record AddressFilteredMessage(UUID crawlerId, List<URL> address) {
}
