import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Search, Plus } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Button } from '../components/common/Button'
import { Input } from '../components/common/Input'
import { Loader } from '../components/common/Loader'
import { EmptyState } from '../components/common/EmptyState'
import { Alert } from '../components/common/Alert'
import { Pagination } from '../components/common/Pagination'
import { QuestionCard } from '../components/qa/QuestionCard'
import { listQuestions, searchQuestions } from '../api/questionApi'
import { getApiErrorMessage } from '../api/axios'
import { useAuth } from '../hooks/useAuth'

export function QuestionsPage() {
  const { isAuthed } = useAuth()
  const navigate = useNavigate()
  const [sp, setSp] = useSearchParams()

  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [items, setItems] = useState([])

  const page = Number(sp.get('page') || 0)
  const size = Number(sp.get('size') || 20)
  const keyword = (sp.get('keyword') || '').trim()

  const hasNext = useMemo(() => (items?.length || 0) >= size, [items, size])

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = keyword
          ? await searchQuestions({ keyword, page, size })
          : await listQuestions({ page, size })
        if (alive) setItems(data || [])
      } catch (e) {
        if (alive) setErr(getApiErrorMessage(e))
      } finally {
        if (alive) setLoading(false)
      }
    })()
    return () => {
      alive = false
    }
  }, [keyword, page, size])

  const setParam = (next) => {
    const merged = new URLSearchParams(sp)
    Object.entries(next).forEach(([k, v]) => {
      if (v === undefined || v === null || v === '') merged.delete(k)
      else merged.set(k, String(v))
    })
    setSp(merged, { replace: true })
  }

  return (
    <PageContainer
      title="Q&A Forum"
      subtitle="Browse questions, search by keyword, and ask your own."
      actions={
        <Button
          size="sm"
          onClick={() => (isAuthed ? navigate('/ask') : navigate('/login', { state: { from: '/ask' } }))}
          className="gap-1.5"
        >
          <Plus className="h-4 w-4" /> Ask
        </Button>
      }
    >
      <div className="grid gap-5">
        <div className="cc-card p-4">
          <div className="grid gap-3 sm:grid-cols-[1fr_auto_auto] sm:items-end">
            <Input
              label="Search"
              placeholder='Try: "spring security"'
              value={keyword}
              onChange={(e) => setParam({ keyword: e.target.value, page: 0 })}
            />
            <Button variant="secondary" className="gap-2" onClick={() => setParam({ keyword, page: 0 })}>
              <Search className="h-4 w-4" /> Search
            </Button>
            <Button variant="ghost" onClick={() => setParam({ keyword: '', page: 0 })}>
              Clear
            </Button>
          </div>
        </div>

        {err ? <Alert tone="error" title="Could not load questions">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading questions" />
        ) : items?.length ? (
          <div className="grid gap-3">
            {items.map((q) => (
              <QuestionCard key={q.id} q={q} />
            ))}
          </div>
        ) : (
          <EmptyState title="No questions found" hint={keyword ? 'Try a different keyword.' : 'Be the first to ask.'} />
        )}

        <Pagination
          page={page}
          hasNext={hasNext}
          onPrev={() => setParam({ page: Math.max(0, page - 1) })}
          onNext={() => setParam({ page: page + 1 })}
          className="pt-2"
        />
      </div>
    </PageContainer>
  )
}
