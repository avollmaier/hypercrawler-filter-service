package at.hypercrawler.filterservice.filter.domain.service;

import java.net.URL;
import java.util.List;
import java.util.function.Function;

import at.hypercrawler.filterservice.filter.domain.util.UrlParser;

public class AddressListDuplicationChecker
  implements Function<List<URL>, List<URL>> {

  @Override
  public List<URL> apply(List<URL> urls) {
    if (urls == null || urls.isEmpty()) {
      return List.of();
    }
    return urls.stream().map(URL::toString).distinct().map(UrlParser::parseUrl).toList();
  }
}
