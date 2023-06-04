package at.hypercrawler.filterservice.manager;

import lombok.Builder;


@Builder
public record CrawlerRobotOptions(
        boolean ignoreRobotRules,
        boolean ignoreRobotNoIndex,
        boolean ignoreRobotNoFollowTo
) {
}
