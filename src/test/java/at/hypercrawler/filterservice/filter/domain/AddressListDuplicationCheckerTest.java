package at.hypercrawler.filterservice.filter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.hypercrawler.filterservice.filter.domain.service.AddressListDuplicationChecker;

public class AddressListDuplicationCheckerTest {

  private AddressListDuplicationChecker duplicationChecker;

  @BeforeEach
  public void setUp() {
    duplicationChecker = new AddressListDuplicationChecker();
  }

  @Test
  public void whenApplyWithNullInput_thenEmptyListReturned() {
    List<URL> urls = null;

    List<URL> result = duplicationChecker.apply(urls);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void whenApplyWithEmptyInput_thenEmptyListReturned() {
    List<URL> urls = new ArrayList<>();

    List<URL> result = duplicationChecker.apply(urls);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void whenApplyWithNoDuplicates_thenSameListReturned()
    throws MalformedURLException {
    List<URL> urls = Arrays.asList(new URL("http://example.com"), new URL("https://google.com"),
      new URL("http://example.org"));

    List<URL> result = duplicationChecker.apply(urls);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(urls, result);
  }

  @Test
  public void whenApplyWithDuplicates_thenDuplicatesRemoved()
    throws MalformedURLException {
    URL url1 = new URL("http://example.com");
    URL url2 = new URL("https://google.com");
    URL url3 = new URL("http://example.org");
    URL url4 = new URL("https://google.com");

    List<URL> urls = Arrays.asList(url1, url2, url3, url4);

    List<URL> result = duplicationChecker.apply(urls);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(Arrays.asList(url1, url2, url3), result);
  }

  @Test
  public void whenApplyWithLargeListOfDuplicates_thenDuplicatesRemoved()
    throws MalformedURLException {
    URL url1 = new URL("http://example.com");
    URL url2 = new URL("https://google.com");

    List<URL> urls = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      urls.add(url1);
      urls.add(url2);
    }

    List<URL> result = duplicationChecker.apply(urls);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(Arrays.asList(url1, url2), result);
  }

}
