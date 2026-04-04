import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { CheckCircle2, MessageSquareText } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Loader } from '../components/common/Loader'
import { Alert } from '../components/common/Alert'
import { EmptyState } from '../components/common/EmptyState'
import { Button } from '../components/common/Button'
import { Textarea } from '../components/common/Textarea'
import { TagBadge } from '../components/qa/TagBadge'
import { AnswerCard } from '../components/qa/AnswerCard'
import { formatDateTime } from '../utils/formatDate'
import { getApiErrorMessage } from '../api/axios'
import { acceptAnswer, addAnswer, getQuestion, voteAnswer } from '../api/questionApi'
import { useAuth } from '../hooks/useAuth'

function sortAnswersOldestFirst(list) {
  return [...(list || [])].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
}

export function QuestionDetailPage() {
  const { id } = useParams()
  const { isAuthed, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [q, setQ] = useState(null)
  const [answerText, setAnswerText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [acting, setActing] = useState(false)
  const [voteByAnswerId, setVoteByAnswerId] = useState({})

  const isOwner = useMemo(() => Boolean(user?.id && q?.user?.id && user.id === q.user.id), [user, q])

  const refresh = async () => {
    const data = await getQuestion(id)
    setQ({ ...data, answers: sortAnswersOldestFirst(data.answers) })
  }

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await getQuestion(id)
        if (alive) setQ({ ...data, answers: sortAnswersOldestFirst(data.answers) })
      } catch (e) {
        if (alive) setErr(getApiErrorMessage(e))
      } finally {
        if (alive) setLoading(false)
      }
    })()
    return () => {
      alive = false
    }
  }, [id])

  const onVote = async (answerId, voteType) => {
    if (!isAuthed) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    setActing(true)
    setErr('')
    try {
      const updated = await voteAnswer(answerId, { voteType })
      setQ((prev) => {
        if (!prev) return prev
        return {
          ...prev,
          answers: sortAnswersOldestFirst(prev.answers.map((a) => (a.id === updated.id ? updated : a))),
        }
      })
      setVoteByAnswerId((prev) => {
        const prevVote = prev[answerId] || null
        const nextVote = prevVote === voteType ? null : voteType
        return { ...prev, [answerId]: nextVote }
      })
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setActing(false)
    }
  }

  const onAccept = async (answerId) => {
    if (!isAuthed) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    setActing(true)
    setErr('')
    try {
      await acceptAnswer(answerId)
      await refresh()
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setActing(false)
    }
  }

  const submitAnswer = async () => {
    if (!isAuthed) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    const trimmed = answerText.trim()
    if (trimmed.length < 5) {
      setErr('Answer must be at least 5 characters')
      return
    }
    setErr('')
    setSubmitting(true)
    try {
      const created = await addAnswer(id, { content: trimmed })
      setAnswerText('')
      setQ((prev) => {
        if (!prev) return prev
        return { ...prev, answers: sortAnswersOldestFirst([...(prev.answers || []), created]) }
      })
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <PageContainer title="Question" subtitle="Loading question details">
        <Loader label="Loading question" />
      </PageContainer>
    )
  }

  if (err && !q) {
    return (
      <PageContainer title="Question" subtitle="Could not load question">
        <Alert tone="error" title="Error">{err}</Alert>
      </PageContainer>
    )
  }

  if (!q) return null

  return (
    <PageContainer title={q.title} subtitle={`Asked by ${q.user?.fullName || 'Unknown'} · ${formatDateTime(q.createdAt)}`}>
      <div className="grid gap-6">
        {err ? <Alert tone="error" title="Action failed">{err}</Alert> : null}

        <div className="cc-card p-6">
          {q.description ? (
            <div className="whitespace-pre-wrap text-sm leading-relaxed text-slate-800">{q.description}</div>
          ) : (
            <div className="text-sm text-slate-600">No description provided.</div>
          )}

          {q.tags?.length ? (
            <div className="mt-4 flex flex-wrap gap-2">
              {q.tags.map((t) => (
                <TagBadge key={t} tag={t} />
              ))}
            </div>
          ) : null}
        </div>

        <div className="flex items-end justify-between gap-3">
          <div>
            <div className="text-lg font-extrabold text-slate-900">Answers</div>
            <div className="text-sm text-slate-600">Vote for helpful answers. Accepted answers are highlighted.</div>
          </div>
          <div className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600">
            <MessageSquareText className="h-4 w-4 text-indigo-700" />
            {(q.answers || []).length} total
          </div>
        </div>

        {q.answers?.length ? (
          <div className="grid gap-3">
            {q.answers.map((a) => {
              const canVote = isAuthed && a.user?.id !== user?.id && !acting
              const canAccept = isAuthed && isOwner && !acting
              return (
                <AnswerCard
                  key={a.id}
                  answer={a}
                  canVote={canVote}
                  onVote={(voteType) => onVote(a.id, voteType)}
                  canAccept={canAccept}
                  onAccept={() => onAccept(a.id)}
                  userVote={voteByAnswerId[a.id] || null}
                />
              )
            })}
          </div>
        ) : (
          <EmptyState title="No answers yet" hint="Be the first to help." />
        )}

        <div className="cc-card p-6">
          <div className="flex items-center justify-between gap-3">
            <div>
              <div className="text-base font-extrabold text-slate-900">Your answer</div>
              <div className="text-sm text-slate-600">
                {isAuthed ? 'Write a clear, helpful response.' : 'Login to post an answer.'}
              </div>
            </div>
            {isOwner ? (
              <div className="inline-flex items-center gap-2 rounded-2xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-extrabold text-emerald-800">
                <CheckCircle2 className="h-4 w-4" /> You asked this
              </div>
            ) : null}
          </div>

          <div className="mt-4 grid gap-3">
            <Textarea
              rows={6}
              placeholder="Add your answer..."
              value={answerText}
              onChange={(e) => setAnswerText(e.target.value)}
              disabled={!isAuthed || submitting}
            />
            <div className="flex justify-end">
              <Button onClick={submitAnswer} loading={submitting} disabled={!isAuthed}>
                Post answer
              </Button>
            </div>
          </div>
        </div>
      </div>
    </PageContainer>
  )
}
