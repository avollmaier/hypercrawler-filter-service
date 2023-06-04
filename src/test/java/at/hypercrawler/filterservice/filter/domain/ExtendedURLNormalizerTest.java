package at.hypercrawler.filterservice.filter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.hypercrawler.filterservice.filter.domain.service.ExtendedURLNormalizer;

public class ExtendedURLNormalizerTest {

  private ExtendedURLNormalizer urlNormalizer;

  @BeforeEach
  public void setUp() {
    urlNormalizer = new ExtendedURLNormalizer();
  }

  @Test
  public void whenApplyWithNullInput_thenEmptyListReturned() {
    List<String> rawAddresses = null;

    List<URL> result = urlNormalizer.apply(rawAddresses);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void whenApplyWithEmptyInput_thenEmptyListReturned() {
    List<String> rawAddresses = Collections.emptyList();

    List<URL> result = urlNormalizer.apply(rawAddresses);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void whenApplyWithValidAddresses_thenUrlsReturned() {
    List<String> rawAddresses = Arrays.asList("http://example.com", "https://google.com");

    List<URL> result = urlNormalizer.apply(rawAddresses);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("http://example.com/", result.get(0).toString());
    assertEquals("https://google.com/", result.get(1).toString());
  }

  @Test
  public void whenApplyWithMixedValidAndInvalidAddresses_thenNormalizedUrlsReturned() {
    List<String> rawAddresses = Arrays.asList("http://example.com", "not_a_valid_url", "https://google.com");

    List<URL> result = urlNormalizer.apply(rawAddresses);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("http://example.com/", result.get(0).toString());
    assertEquals("http://not_a_valid_url/", result.get(1).toString());
    assertEquals("https://google.com/", result.get(2).toString());
  }

}