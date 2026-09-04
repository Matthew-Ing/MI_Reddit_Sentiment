package com.mi.backend.scrape;

import com.mi.backend.domain.Subreddit;
import com.mi.backend.domain.SubredditRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubredditSeeder {

  @Bean
  ApplicationRunner seedEngineeringResumes(SubredditRepository repo) {
    return args -> repo.findByNameIgnoreCase("EngineeringResumes")
        .orElseGet(() -> {
          var s = new Subreddit();
          s.setName("EngineeringResumes");
          s.setEnabled(true);
          return repo.save(s);
        });
  }
}