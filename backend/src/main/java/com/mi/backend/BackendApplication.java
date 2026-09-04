package com.mi.backend;

import com.mi.backend.config.AwsProperties;
import com.mi.backend.reddit.RedditProperties;
import com.mi.backend.sentiment.SentimentProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableConfigurationProperties({AwsProperties.class, RedditProperties.class, SentimentProperties.class})
@EnableScheduling
public class BackendApplication {

  public static void main(String[] args) {
    loadDotEnv();
    SpringApplication.run(BackendApplication.class, args);
  }

  private static void loadDotEnv() {
    Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
    for (int i = 0; i < 4 && dir != null; i++) {
      if (Files.isRegularFile(dir.resolve(".env"))) {
        Dotenv.configure()
            .directory(dir.toString())
            .ignoreIfMalformed()
            .load()
            .entries()
            .forEach(e -> {
              if (System.getenv(e.getKey()) == null && System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
              }
            });
        return;
      }
      dir = dir.getParent();
    }
  }
}