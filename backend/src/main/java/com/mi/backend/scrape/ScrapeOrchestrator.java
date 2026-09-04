package com.mi.backend.scrape;

import com.mi.backend.domain.Post;
import com.mi.backend.domain.PostRepository;
import com.mi.backend.domain.ScrapeRun;
import com.mi.backend.domain.ScrapeRunRepository;
import com.mi.backend.domain.ScrapeStatus;
import com.mi.backend.domain.Subreddit;
import com.mi.backend.domain.SubredditRepository;
import com.mi.backend.reddit.RedditApiClient;
import com.mi.backend.reddit.RedditListing;
import com.mi.backend.sentiment.SentimentService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class ScrapeOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ScrapeOrchestrator.class);

    private final SubredditRepository subreddits;
    private final ScrapeRunRepository scrapeRuns;
    private final PostRepository posts;
    private final RedditApiClient reddit;
    private final RawPostArchive archive;
    private final JsonMapper mapper;

    private final SentimentService sentiment;

    public ScrapeOrchestrator(
            SubredditRepository subreddits,
            ScrapeRunRepository scrapeRuns,
            PostRepository posts,
            RedditApiClient reddit,
            RawPostArchive archive,
            JsonMapper mapper,
            SentimentService sentiment) {
        this.subreddits = subreddits;
        this.scrapeRuns = scrapeRuns;
        this.posts = posts;
        this.reddit = reddit;
        this.archive = archive;
        this.mapper = mapper;
        this.sentiment = sentiment;
    }

    public void runDaily() {
        var enabled = subreddits.findByEnabledTrue();
        log.info("Daily scrape starting for {} subreddit(s)", enabled.size());
        for (Subreddit sub : enabled) {
            scrapeOne(sub);
        }
    }

    private void scrapeOne(Subreddit sub) {
        LocalDate day = LocalDate.now(ZoneOffset.UTC);
        if (alreadyScrapedToday(sub, day)) {
            log.info("Skipping r/{} — already scraped on {}", sub.getName(), day);
            return;
        }

        var run = new ScrapeRun();
        run.setSubreddit(sub.getName());
        run.setStatus(ScrapeStatus.RUNNING);
        run.setStartedAt(Instant.now());
        scrapeRuns.save(run);

        try {
            var fetched = reddit.fetchTopDay(sub.getName());
            List<Post> saved = new ArrayList<>();
            int upserted = 0;
            for (RedditListing.PostData data : fetched) {
                if (data.id() == null || data.id().isBlank()) {
                    continue;
                }
                saved.add(upsert(sub.getName(), day, data));
                upserted++;
            }
            run.setStatus(ScrapeStatus.SUCCESS);
            run.setPostsUpserted(upserted);
            run.setApiCalls(1);
            sub.setLastScrapedAt(Instant.now());
            subreddits.save(sub);
            log.info("Scraped r/{}: {} posts", sub.getName(), upserted);
            sentiment.scoreAndRollup(sub.getName(), day, saved);
        } catch (Exception e) {
            run.setStatus(ScrapeStatus.FAILED);
            run.setError(e.getMessage());
            log.warn("Scrape failed for r/{}: {}", sub.getName(), e.getMessage());
        } finally {
            run.setFinishedAt(Instant.now());
            scrapeRuns.save(run);
        }
    }

    private Post upsert(String subreddit, LocalDate day, RedditListing.PostData data) {
        Post post = posts.findByRedditId(data.id()).orElseGet(Post::new);
        post.setRedditId(data.id());
        post.setSubreddit(subreddit);
        post.setTitle(data.title());
        post.setAuthor(data.author());
        post.setScore(data.score());
        post.setNumComments(data.numComments());
        post.setCreatedUtc(data.createdAt());
        post.setPermalink(data.permalink());
        post.setSelftextExcerpt(data.excerpt());
        String json = mapper.writeValueAsString(data);
        post.setS3Key(archive.put(subreddit, day, data.id(), json));
        return posts.save(post);
    }

    private static boolean alreadyScrapedToday(Subreddit sub, LocalDate day) {
        Instant last = sub.getLastScrapedAt();
        if (last == null) {
            return false;
        }
        return last.atZone(ZoneOffset.UTC).toLocalDate().equals(day);
    }
}