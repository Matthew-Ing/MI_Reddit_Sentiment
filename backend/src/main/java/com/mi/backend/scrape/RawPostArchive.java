package com.mi.backend.scrape;

import com.mi.backend.config.AwsProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDate;

@Component
public class RawPostArchive {

  private final S3Client s3;
  private final AwsProperties props;

  public RawPostArchive(S3Client s3, AwsProperties props) {
    this.s3 = s3;
    this.props = props;
  }

  public String put(String subreddit, LocalDate date, String redditId, String json) {
    String key = "raw/subreddit=%s/dt=%s/post=%s.json"
        .formatted(subreddit, date, redditId);
    s3.putObject(
        b -> b.bucket(props.s3().bucket()).key(key),
        RequestBody.fromString(json));
    return key;
  }
}