import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CheckCircle2, Trash2 } from 'lucide-react'
import { deleteAdminAnswer, deleteAdminQuestion, getAdminQuestion } from '../api/adminApi'
import { getApiErrorMessage } from '../api/axios'
import { AdminLayout } from '../components/admin/AdminLayout'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { Button } from '../components/common/Button'
import { EmptyState } from '../components/common/EmptyState'
import { Loader } from '../components/common/Loader'
import { Modal } from '../components/common/Modal'
import { formatDateTime } from '../utils/formatDate'
import { useToast } from '../hooks/useToast'

export function AdminQuestionDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [question, setQuestion] = useState(null)
  const [confirmState, setConfirmState] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setErr('')
    try {
      const data = await getAdminQuestion(id)
      setQuestion(data)
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    load()
  }, [load])

  const handleDelete = async () => {
    if (!confirmState) return
    setDeleting(true)
    setErr('')
    try {
      if (confirmState.kind === 'question') {
        await deleteAdminQuestion(question.id)
        showToast({ tone: 'success', title: 'Question deleted', message: question.title })
        navigate('/admin/questions', { replace: true })
        return
      }

      await deleteAdminAnswer(confirmState.answer.id)
      setQuestion((prev) => ({
        ...prev,
        answers: (prev?.answers || []).filter((item) => item.id !== confirmState.answer.id),
      }))
      showToast({ tone: 'success', title: 'Answer deleted', message: `Answer #${confirmState.answer.id}` })
      setConfirmState(null)
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Delete failed', message })
    } finally {
      setDeleting(false)
    }
  }

  return (
    <AdminLayout
      title="Question Detail"
      subtitle="Inspect question content and remove answers individually if needed."
      actions={
        <Button variant="danger" onClick={() => setConfirmState({ kind: 'question' })} disabled={loading || !question}>
          Delete Question
        </Button>
      }
    >
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not load question">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading question" />
        ) : question ? (
          <>
            <div className="cc-card p-6">
              <div className="text-2xl font-extrabold tracking-tight text-slate-900">{question.title}</div>
              <div className="mt-2 text-sm text-slate-600">
                By {question.author?.fullName || 'Unknown'} · {formatDateTime(question.createdAt)}
              </div>
              <div className="mt-4 whitespace-pre-wrap text-sm leading-relaxed text-slate-800">
                {question.description || 'No description provided.'}
              </div>
              {question.tags?.length ? (
                <div className="mt-4 flex flex-wrap gap-2">
                  {question.tags.map((tag) => (
                    <Badge key={tag} tone="blue">{tag}</Badge>
                  ))}
                </div>
              ) : null}
            </div>

            {(question.answers || []).length ? (
              <div className="grid gap-3">
                {question.answers.map((answer) => (
                  <div
                    key={answer.id}
                    className={`cc-card p-5 ${answer.isAccepted ? 'border-emerald-200 bg-emerald-50/40' : ''}`}
                  >
                    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <div className="text-sm font-extrabold text-slate-900">{answer.author?.fullName || 'Unknown'}</div>
                          <div className="text-xs font-semibold text-slate-500">{formatDateTime(answer.createdAt)}</div>
                          {answer.isAccepted ? (
                            <Badge tone="green" className="gap-1">
                              <CheckCircle2 className="h-3.5 w-3.5" /> Accepted
                            </Badge>
                          ) : null}
                        </div>
                        <div className="mt-3 whitespace-pre-wrap text-sm leading-relaxed text-slate-800">
                          {answer.content}
                        </div>
                        <div className="mt-4 flex flex-wrap gap-2 text-xs font-semibold text-slate-600">
                          <Badge tone="slate">Upvotes {answer.upvoteCount ?? 0}</Badge>
                          <Badge tone="slate">Downvotes {answer.downvoteCount ?? 0}</Badge>
                          <Badge tone={(answer.score || 0) >= 0 ? 'indigo' : 'red'}>Score {answer.score ?? 0}</Badge>
                        </div>
                      </div>

                      <Button
                        size="sm"
                        variant="danger"
                        onClick={() => setConfirmState({ kind: 'answer', answer })}
                        className="shrink-0 gap-1.5"
                      >
                        <Trash2 className="h-4 w-4" /> Delete
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState title="No answers left" hint="This question currently has no answers to moderate." />
            )}
          </>
        ) : null}
      </div>

      <Modal
        open={Boolean(confirmState)}
        title={confirmState?.kind === 'question' ? 'Delete question?' : 'Delete answer?'}
        subtitle={
          confirmState?.kind === 'question'
            ? 'This removes the full thread and all related votes.'
            : 'This removes the answer and all votes attached to it.'
        }
        onClose={() => (deleting ? null : setConfirmState(null))}
        footer={
          <>
            <Button variant="ghost" onClick={() => setConfirmState(null)} disabled={deleting}>Cancel</Button>
            <Button variant="danger" onClick={handleDelete} loading={deleting}>
              {confirmState?.kind === 'question' ? 'Delete question' : 'Delete answer'}
            </Button>
          </>
        }
      >
        <div className="text-sm text-slate-700">
          {confirmState?.kind === 'question' ? question?.title : confirmState?.answer?.content}
        </div>
      </Modal>
    </AdminLayout>
  )
}
