export type Subreddit = {
    id: number
    name: string
    enabled: boolean
    lastScrapedAt: string | null
  }
  
  export type Post = {
    redditId: string
    subreddit: string
    title: string
    author: string
    score: number
    numComments: number
    createdUtc: string
    permalink: string
    selftextExcerpt: string | null
    sentimentLabel: string | null
    sentimentScore: number | null
    sentimentRationale: string | null
  }
  
  export type DailySentiment = {
    subreddit: string
    date: string
    postCount: number
    avgScore: number
    weightedScore: number
    label: string
    summary: string | null
    model: string | null
    computedAt: string
  }
  
  export type ScrapeRun = {
    id: number
    subreddit: string
    status: string
    startedAt: string | null
    finishedAt: string | null
    postsUpserted: number
    apiCalls: number
    error: string | null
  }