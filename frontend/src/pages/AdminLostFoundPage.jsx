import { useEffect, useState } from 'react'
import { deleteAdminLostFound, listAdminLostFound } from '../api/adminApi'
import { getApiErrorMessage } from '../api/axios'
import { AdminLayout } from '../components/admin/AdminLayout'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { Button } from '../components/common/Button'
import { EmptyState } from '../components/common/EmptyState'
import { Loader } from '../components/common/Loader'
import { Modal } from '../components/common/Modal'
import { formatDate, formatDateTime } from '../utils/formatDate'
import { useToast } from '../hooks/useToast'

function toneForType(type) {
  return type === 'FOUND' ? 'blue' : 'red'
}

function toneForStatus(status) {
  if (status === 'RESOLVED') return 'green'
  if (status === 'CLAIMED') return 'amber'
  return 'slate'
}

export function AdminLostFoundPage() {
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [posts, setPosts] = useState([])
  const [confirmPost, setConfirmPost] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const load = async () => {
    setLoading(true)
    setErr('')
    try {
      const data = await listAdminLostFound()
      setPosts(data || [])
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
    if (!confirmPost) return
    setDeleting(true)
    setErr('')
    try {
      await deleteAdminLostFound(confirmPost.id)
      setPosts((prev) => prev.filter((item) => item.id !== confirmPost.id))
      showToast({ tone: 'success', title: 'Post deleted', message: confirmPost.title })
      setConfirmPost(null)
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Delete failed', message })
    } finally {
      setDeleting(false)
    }
  }

  return (
    <AdminLayout title="Lost & Found" subtitle="Review all posts and remove any item with its related claims.">
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not load posts">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading lost & found posts" />
        ) : posts.length ? (
          <div className="cc-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50 text-left text-slate-600">
                  <tr>
                    <th className="px-4 py-3 font-semibold">Post</th>
                    <th className="px-4 py-3 font-semibold">Owner</th>
                    <th className="px-4 py-3 font-semibold">Location</th>
                    <th className="px-4 py-3 font-semibold">Incident Date</th>
                    <th className="px-4 py-3 font-semibold">Created</th>
                    <th className="px-4 py-3 font-semibold text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200/80 bg-white">
                  {posts.map((item) => (
                    <tr key={item.id}>
                      <td className="px-4 py-4 align-top">
                        <div className="max-w-md font-extrabold text-slate-900">{item.title}</div>
                        <div className="mt-2 flex flex-wrap gap-2">
                          <Badge tone={toneForType(item.type)}>{item.type}</Badge>
                          <Badge tone={toneForStatus(item.status)}>{item.status}</Badge>
                        </div>
                      </td>
                      <td className="px-4 py-4 align-top text-slate-700">{item.owner?.fullName || 'Unknown'}</td>
                      <td className="px-4 py-4 align-top text-slate-700">{item.location}</td>
                      <td className="px-4 py-4 align-top text-slate-700">{formatDate(item.dateOfIncident)}</td>
                      <td className="px-4 py-4 align-top text-slate-600">{formatDateTime(item.createdAt)}</td>
                      <td className="px-4 py-4 align-top text-right">
                        <div className="flex justify-end gap-2">
                          <Button size="sm" variant="secondary" onClick={() => window.open(`/lost-found/${item.id}`, '_blank')}>
                            Open
                          </Button>
                          <Button size="sm" variant="danger" onClick={() => setConfirmPost(item)}>
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
          <EmptyState title="No lost & found posts" hint="Posts will appear here once users start reporting items." />
        )}
      </div>

      <Modal
        open={Boolean(confirmPost)}
        title="Delete lost & found post?"
        subtitle="This removes the post and any related claim requests."
        onClose={() => (deleting ? null : setConfirmPost(null))}
        footer={
          <>
            <Button variant="ghost" onClick={() => setConfirmPost(null)} disabled={deleting}>Cancel</Button>
            <Button variant="danger" onClick={confirmDelete} loading={deleting}>Delete post</Button>
          </>
        }
      >
        <div className="text-sm text-slate-700">{confirmPost?.title}</div>
      </Modal>
    </AdminLayout>
  )
}
