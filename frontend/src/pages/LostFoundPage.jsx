import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Filter, Plus } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Button } from '../components/common/Button'
import { Loader } from '../components/common/Loader'
import { EmptyState } from '../components/common/EmptyState'
import { Alert } from '../components/common/Alert'
import { Pagination } from '../components/common/Pagination'
import { LostFoundCard } from '../components/lostfound/LostFoundCard'
import { listLostFound } from '../api/lostFoundApi'
import { getApiErrorMessage } from '../api/axios'
import { useAuth } from '../hooks/useAuth'

export function LostFoundPage() {
  const { isAuthed } = useAuth()
  const navigate = useNavigate()
  const [sp, setSp] = useSearchParams()

  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [items, setItems] = useState([])

  const page = Number(sp.get('page') || 0)
  const size = Number(sp.get('size') || 20)
  const type = sp.get('type') || ''
  const status = sp.get('status') || ''

  const hasNext = useMemo(() => (items?.length || 0) >= size, [items, size])

  const setParam = (next) => {
    const merged = new URLSearchParams(sp)
    Object.entries(next).forEach(([k, v]) => {
      if (v === undefined || v === null || v === '') merged.delete(k)
      else merged.set(k, String(v))
    })
    setSp(merged, { replace: true })
  }

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await listLostFound({ type: type || undefined, status: status || undefined, page, size })
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
  }, [type, status, page, size])

  return (
    <PageContainer
      title="Lost & Found"
      subtitle="Report lost or found items with a location and incident date."
      actions={
        <Button
          size="sm"
          onClick={() => (isAuthed ? navigate('/lost-found/new') : navigate('/login', { state: { from: '/lost-found/new' } }))}
          className="gap-1.5"
        >
          <Plus className="h-4 w-4" /> Create
        </Button>
      }
    >
      <div className="grid gap-5">
        <div className="cc-card p-4">
          <div className="grid gap-3 md:grid-cols-3 md:items-end">
            <label className="block">
              <div className="mb-1.5 text-sm font-semibold text-slate-700">Type</div>
              <select
                value={type}
                onChange={(e) => setParam({ type: e.target.value, page: 0 })}
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-900 shadow-sm outline-none transition focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
              >
                <option value="">All</option>
                <option value="LOST">LOST</option>
                <option value="FOUND">FOUND</option>
              </select>
            </label>
            <label className="block">
              <div className="mb-1.5 text-sm font-semibold text-slate-700">Status</div>
              <select
                value={status}
                onChange={(e) => setParam({ status: e.target.value, page: 0 })}
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-900 shadow-sm outline-none transition focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
              >
                <option value="">All</option>
                <option value="OPEN">OPEN</option>
                <option value="CLAIMED">CLAIMED</option>
                <option value="RESOLVED">RESOLVED</option>
              </select>
            </label>
            <div className="flex items-end justify-between gap-2">
              <Button variant="secondary" className="gap-2" onClick={() => setParam({ type, status, page: 0 })}>
                <Filter className="h-4 w-4" /> Apply
              </Button>
              <Button variant="ghost" onClick={() => setParam({ type: '', status: '', page: 0 })}>
                Clear
              </Button>
            </div>
          </div>
        </div>

        {err ? <Alert tone="error" title="Could not load posts">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading posts" />
        ) : items?.length ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((p) => (
              <LostFoundCard key={p.id} post={p} />
            ))}
          </div>
        ) : (
          <EmptyState title="No posts found" hint="Try adjusting filters or create a new post." />
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
