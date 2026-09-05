import { useEffect, useState } from 'react'
import { api, utcToday } from './api'
import type { DailySentiment, Post, Subreddit } from './types'
import './App.css'

type Page = 'dashboard' | 'posts' | 'history'

export default function App() {
  const [page, setPage] = useState<Page>('dashboard')
  const [subs, setSubs] = useState<Subreddit[]>([])
  const [sub, setSub] = useState('EngineeringResumes')
  const [date] = useState(utcToday)
  const [mood, setMood] = useState<DailySentiment | null>(null)
  const [posts, setPosts] = useState<Post[]>([])
  const [history, setHistory] = useState<DailySentiment[]>([])
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api.subreddits().then((list) => {
      setSubs(list)
      if (list.length && !list.some((s) => s.name === sub)) {
        setSub(list[0].name)
      }
    }).catch((e) => setError(String(e)))
  }, [])

  useEffect(() => {
    setError(null)
    const from = new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10)
    Promise.all([
      api.daily(sub, from, date),
      api.posts(sub, date),
    ])
      .then(([days, list]) => {
        setHistory(days)
        setMood(days.find((d) => d.date === date) ?? days.at(-1) ?? null)
        setPosts(list)
      })
      .catch((e) => setError(String(e)))
  }, [sub, date])

  async function analyze() {
    setBusy(true)
    setError(null)
    try {
      await api.enqueue()
      await new Promise((r) => setTimeout(r, 2000))
      const from = new Date(Date.now() - 14 * 86400000).toISOString().slice(0, 10)
      const [days, list] = await Promise.all([
        api.daily(sub, from, date),
        api.posts(sub, date),
      ])
      setHistory(days)
      setMood(days.find((d) => d.date === date) ?? days.at(-1) ?? null)
      setPosts(list)
    } catch (e) {
      setError(String(e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Subreddit mood</h1>
        <nav>
          <button className={page === 'dashboard' ? 'on' : ''} onClick={() => setPage('dashboard')}>Dashboard</button>
          <button className={page === 'posts' ? 'on' : ''} onClick={() => setPage('posts')}>Top posts</button>
          <button className={page === 'history' ? 'on' : ''} onClick={() => setPage('history')}>History</button>
        </nav>
        <label>
          Subreddit
          <select value={sub} onChange={(e) => setSub(e.target.value)}>
            {subs.map((s) => (
              <option key={s.id} value={s.name}>{s.name}</option>
            ))}
          </select>
        </label>
      </header>

      {error && <p className="err">{error}</p>}

      {page === 'dashboard' && (
        <section>
          <p className="muted">{date} (UTC)</p>
          {mood ? (
            <>
              <p className={`label ${mood.label.toLowerCase()}`}>{mood.label}</p>
              <p>Weighted score {mood.weightedScore.toFixed(2)} · {mood.postCount} posts</p>
              <p>{mood.summary}</p>
            </>
          ) : (
            <p>No daily sentiment for this date yet.</p>
          )}
          <button type="button" disabled={busy} onClick={analyze}>
            {busy ? 'Queuing…' : 'Analyze today'}
          </button>
          <p className="muted">Enqueues SQS. If already scraped today, the worker skips RapidAPI.</p>
        </section>
      )}

      {page === 'posts' && (
        <section>
          {posts.length === 0 && <p>No posts for {date}.</p>}
          <ul className="posts">
            {posts.map((p) => (
              <li key={p.redditId}>
                <strong>{p.sentimentLabel ?? '—'}</strong>{' '}
                <a href={p.permalink?.startsWith('http') ? p.permalink : `https://reddit.com${p.permalink}`} target="_blank" rel="noreferrer">
                  {p.title}
                </a>
                <span className="muted"> score {p.score} · {p.author}</span>
                {p.sentimentRationale && <div className="muted">{p.sentimentRationale}</div>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {page === 'history' && (
        <section>
          <table>
            <thead>
              <tr><th>Date</th><th>Label</th><th>Weighted</th><th>Posts</th></tr>
            </thead>
            <tbody>
              {history.map((d) => (
                <tr key={d.date}>
                  <td>{d.date}</td>
                  <td>{d.label}</td>
                  <td>{d.weightedScore.toFixed(2)}</td>
                  <td>{d.postCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </div>
  )
}