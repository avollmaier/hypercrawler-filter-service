package at.hypercrawler.filterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FilterApplication {


  public static void main(String[] args) {
    SpringApplication.run(FilterApplication.class, args);
  }
}
