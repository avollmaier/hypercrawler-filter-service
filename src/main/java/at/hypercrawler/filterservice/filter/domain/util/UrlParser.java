package at.hypercrawler.filterservice.filter.domain.util;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UrlParser {
  public static URL parseUrl(String address) {
    try {
      return new URL(address);
    }
    catch (MalformedURLException e) {
      log.warn("Could not parse address {}", address);
      return null;
    }
  }
}
