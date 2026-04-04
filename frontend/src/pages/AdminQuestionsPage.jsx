import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deleteAdminQuestion, listAdminQuestions } from '../api/adminApi'
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

export function AdminQuestionsPage() {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [questions, setQuestions] = useState([])
  const [confirmQuestion, setConfirmQuestion] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const load = async () => {
    setLoading(true)
    setErr('')
    try {
      const data = await listAdminQuestions()
      setQuestions(data || [])
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const confirmDelete = async () => {
    if (!confirmQuestion) return
    setDeleting(true)
    setErr('')
    try {
      await deleteAdminQuestion(confirmQuestion.id)
      setQuestions((prev) => prev.filter((item) => item.id !== confirmQuestion.id))
      showToast({ tone: 'success', title: 'Question deleted', message: confirmQuestion.title })
      setConfirmQuestion(null)
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Delete failed', message })
    } finally {
      setDeleting(false)
    }
  }

  return (
    <AdminLayout title="Questions" subtitle="Inspect questions, open answer threads, and remove content when needed.">
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not load questions">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading questions" />
        ) : questions.length ? (
          <div className="cc-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50 text-left text-slate-600">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Question</th>
                    <th className="px-4 py-3 font-semibold">Author</th>
                    <th className="px-4 py-3 font-semibold">Tags</th>
                    <th className="px-4 py-3 font-semibold">Answers</th>
                    <th className="px-4 py-3 font-semibold">Created</th>
                    <th className="px-4 py-3 font-semibold text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200/80 bg-white">
                  {questions.map((item) => (
                    <tr key={item.id}>
                      <td className="px-4 py-4 align-top">
                        <div className="max-w-md font-extrabold text-slate-900">{item.title}</div>
                      </td>
                      <td className="px-4 py-4 align-top text-slate-700">{item.author?.fullName || 'Unknown'}</td>
                      <td className="px-4 py-4 align-top">
                        <div className="flex max-w-xs flex-wrap gap-2">
                          {(item.tags || []).map((tag) => (
                            <Badge key={tag} tone="blue">{tag}</Badge>
                          ))}
                        </div>
                      </td>
                      <td className="px-4 py-4 align-top font-semibold text-slate-800">{item.answerCount ?? 0}</td>
                      <td className="px-4 py-4 align-top text-slate-600">{formatDateTime(item.createdAt)}</td>
                      <td className="px-4 py-4 align-top text-right">
                        <div className="flex justify-end gap-2">
                          <Button size="sm" variant="secondary" onClick={() => navigate(`/admin/questions/${item.id}`)}>
                            View
                          </Button>
                          <Button size="sm" variant="danger" onClick={() => setConfirmQuestion(item)}>
                            Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <EmptyState title="No questions found" hint="Questions will appear here once the forum is active." />
        )}
      </div>

      <Modal
        open={Boolean(confirmQuestion)}
        title="Delete question?"
        subtitle="This will also remove answers, votes, and tag links for the selected question."
        onClose={() => (deleting ? null : setConfirmQuestion(null))}
        footer={
          <>
            <Button variant="ghost" onClick={() => setConfirmQuestion(null)} disabled={deleting}>Cancel</Button>
            <Button variant="danger" onClick={confirmDelete} loading={deleting}>Delete question</Button>
          </>
        }
      >
        <div className="text-sm text-slate-700">{confirmQuestion?.title}</div>
      </Modal>
    </AdminLayout>
  )
}
