package at.hypercrawler.filterservice.filter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import at.hypercrawler.filterservice.filter.domain.util.RegexUtil;

public class RegexUtilTest {

  @Test
  public void whenCombineRegexWithNullList_thenReturnEmptyPattern() {
    List<String> regexes = null;
    Pattern result = RegexUtil.combineRegex(regexes);

    assertEquals(Pattern.compile("a^").pattern(), result.pattern());
  }

  @Test
  public void whenCombineRegexWithEmptyList_thenReturnEmptyPattern() {
    List<String> regexes = Collections.emptyList();
    Pattern result = RegexUtil.combineRegex(regexes);

    assertEquals(Pattern.compile("a^").pattern(), result.pattern());
  }

  @Test
  public void whenCombineRegexWithNonEmptyList_thenCombineRegexesWithOROperator() {
    List<String> regexes = Arrays.asList("regex1", "regex2", "regex3");
    Pattern result = RegexUtil.combineRegex(regexes);

    assertEquals(Pattern.compile("regex1|regex2|regex3").pattern(), result.pattern());
  }
}

