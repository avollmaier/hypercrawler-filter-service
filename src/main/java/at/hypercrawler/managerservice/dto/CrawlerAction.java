package at.hypercrawler.managerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerAction {

  @NotBlank(message = "Index name could not be empty") String indexName;

  @NotNull(message = "Paths to match during crawl could not be empty") List<@NotNull(message = "Paths to match during crawl could not be empty") String>
    pathsToMatch;

  List<@NotBlank(message = "Selectors to match during crawl could not be empty") String> selectorsToMatch;

  List<@NotNull(message = "File type to match during crawl could not be empty") SupportedFileType>
    fileTypesToMatch;

}
