import type { DailySentiment, Post, ScrapeRun, Subreddit } from './types'

const base = import.meta.env.VITE_API_BASE_URL ?? ''

async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(`${base}${path}`)
  if (!res.ok) throw new Error(`${res.status} ${path}`)
  return res.json() as Promise<T>
}

export function utcToday(): string {
  return new Date().toISOString().slice(0, 10)
}

export const api = {
  subreddits: () => getJson<Subreddit[]>('/api/subreddits'),
  posts: (subreddit: string, date: string) =>
    getJson<Post[]>(
      `/api/posts?subreddit=${encodeURIComponent(subreddit)}&date=${date}`,
    ),
  daily: (subreddit: string, from: string, to: string) =>
    getJson<DailySentiment[]>(
      `/api/sentiment/daily?subreddit=${encodeURIComponent(subreddit)}&from=${from}&to=${to}`,
    ),
  scrapes: () => getJson<ScrapeRun[]>('/api/scrapes'),
  enqueue: async () => {
    const res = await fetch(`${base}/api/scrapes`, { method: 'POST' })
    if (!res.ok) throw new Error(`${res.status} enqueue`)
    return res.json() as Promise<{ status: string }>
  },
}