# Daily Subreddit Sentiment

Spring Boot API + React UI that scrapes a subreddit’s hot posts, scores each post, and rolls that into a **daily mood** (label, weighted score, short summary).

Default seed: `r/EngineeringResumes`.

## How it works

1. UI **Analyze today** (or a scheduler) POSTs `/api/scrapes`.
2. The API enqueues a job on **SQS**.
3. A Spring Boot worker polls the queue, fetches posts via **ReddAPI (RapidAPI)**, upserts them in **Postgres**, and archives raw JSON to **S3**.
4. A stub sentiment classifier labels each post; scores are averaged **weighted by Reddit score**.
5. If that subreddit was already scraped **today (UTC)**, the worker skips the RapidAPI call.

```text
UI ──POST /api/scrapes──► Spring Boot ──SQS──► worker
                              │                    │
                              │                    ├─ RapidAPI (hot listing)
                              │                    ├─ S3 (raw JSON)
                              │                    └─ Postgres (posts + daily mood)
```

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19, TypeScript, Vite (proxy to API on port 8081) |
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA, RestClient |
| Database | PostgreSQL 16 |
| Queue / object store | AWS SDK v2 → SQS + S3 (LocalStack locally) |
| Reddit data | RapidAPI `reddapi.p.rapidapi.com` (`/api/scrape/hot`) |
| Sentiment (local) | Keyword stub (`sentiment.stub: true`) |

**Intended AWS production shape** (not all of this is in-repo yet): EventBridge → SQS → ECS Fargate, RDS, S3, CloudFront, Amazon Bedrock via LangChain4j.

## Repo layout

```
backend/              Spring Boot (Maven wrapper)
frontend/             Vite + React + TypeScript
docker-compose.yaml   Postgres + LocalStack (S3, SQS)
```

## Prerequisites

- Java 21
- Node.js 20+
- Docker Desktop
- A RapidAPI key for [ReddAPI](https://rapidapi.com/) in a root `.env`:

```
RAPIDAPI_KEY=your_key_here
```

The backend loads `.env` from the repo root (or a parent directory) on startup.

## Local run

From the repo root:

```powershell
docker compose up -d
```

Wait until Postgres is healthy, then:

```powershell
cd backend
.\mvnw spring-boot:run
```

API: `http://localhost:8081` (`spring.profiles.active=local`). On start (local profile), the app creates the `sentiment-raw` bucket and `scrape-jobs` queue in LocalStack if they are missing.

```powershell
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173` — Vite proxies `/api` and `/actuator` to the backend.

### Docker Compose services

| Service | Port | Notes |
|---|---|---|
| Postgres | 5432 | DB `sentiment` / user `sentiment` / password `sentiment` |
| LocalStack | 4566 | S3 + SQS |

## API

CORS allows `http://localhost:5173`. No auth in v1.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/subreddits` | List tracked subreddits |
| POST | `/api/subreddits` | Add (`{"name":"cscareerquestions"}`) |
| PATCH | `/api/subreddits/{id}` | Enable/disable (`{"enabled":true}`) |
| POST | `/api/scrapes` | Enqueue daily scrape (202) |
| GET | `/api/scrapes` | Scrape-run history |
| GET | `/api/posts?subreddit=&date=` | Posts for a UTC day |
| GET | `/api/sentiment/daily?subreddit=&from=&to=` | Daily mood history |
| GET | `/actuator/health` | Health |

`date` / `from` / `to` are `yyyy-MM-dd` in UTC.

## UI

- **Dashboard** — today’s label, weighted score, post count, summary; **Analyze today**
- **Top posts** — that day’s posts with per-post sentiment
- **History** — daily rows (date, label, weighted score, post count)

## Data model (Postgres)

- `subreddits` — name, enabled, last scraped
- `scrape_runs` — status, timing, posts upserted, API calls, error
- `posts` — Reddit id (unique), metadata, S3 key, sentiment columns
- `daily_sentiments` — unique `(subreddit, date)`: counts, average, **score-weighted** aggregate, label, summary

S3 keys: `raw/subreddit={name}/dt={yyyy-MM-dd}/post={id}.json`

Daily label: weighted average `> 0.15` → POSITIVE, `< -0.15` → NEGATIVE, else NEUTRAL.

## Out of scope (v1)

- Reddit OAuth Data API
- Comment-tree scraping
- API authentication
- Multi-task worker autoscaling
