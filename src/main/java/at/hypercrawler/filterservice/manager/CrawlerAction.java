package at.hypercrawler.filterservice.manager;

import lombok.Builder;

import java.util.List;

@Builder
public record CrawlerAction(

        String indexName,

        List<String> pathsToMatch,

        List<String> selectorsToMatch,

        List<SupportedContentMediaType> contentTypesToMatch

) {
}
