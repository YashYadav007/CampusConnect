import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Filter, PackageSearch, Plus } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Button } from '../components/common/Button'
import { Input } from '../components/common/Input'
import { Loader } from '../components/common/Loader'
import { EmptyState } from '../components/common/EmptyState'
import { Alert } from '../components/common/Alert'
import { Pagination } from '../components/common/Pagination'
import { MarketplaceCard } from '../components/marketplace/MarketplaceCard'
import { getMarketplaceItems } from '../api/marketplaceApi'
import { getApiErrorMessage } from '../api/axios'
import { useAuth } from '../hooks/useAuth'

export function MarketplacePage() {
  const { isAuthed } = useAuth()
  const navigate = useNavigate()
  const [sp, setSp] = useSearchParams()

  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [items, setItems] = useState([])

  const page = Number(sp.get('page') || 0)
  const size = Number(sp.get('size') || 12)
  const category = (sp.get('category') || '').trim()
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
        const data = await getMarketplaceItems({
          category: category || undefined,
          status: status || undefined,
          page,
          size,
        })
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
  }, [category, status, page, size])

  return (
    <PageContainer
      title="Campus Marketplace"
      subtitle="List useful campus items, browse student listings, and reserve them with a small token payment."
      actions={
        <div className="flex flex-wrap gap-2">
          {isAuthed ? (
            <Button variant="secondary" size="sm" onClick={() => navigate('/marketplace/my-listings')}>
              My Listings
            </Button>
          ) : null}
          <Button
            size="sm"
            onClick={() =>
              isAuthed ? navigate('/marketplace/create') : navigate('/login', { state: { from: '/marketplace/create' } })
            }
            className="gap-1.5"
          >
            <Plus className="h-4 w-4" /> Create Listing
          </Button>
        </div>
      }
    >
      <div className="grid gap-5">
        <div className="cc-card p-4">
          <div className="grid gap-3 md:grid-cols-[1fr_220px_auto_auto] md:items-end">
            <Input
              label="Category"
              placeholder="Try: Electronics"
              value={category}
              onChange={(e) => setParam({ category: e.target.value, page: 0 })}
            />
            <label className="block">
              <div className="mb-1.5 text-sm font-semibold text-slate-700">Status</div>
              <select
                value={status}
                onChange={(e) => setParam({ status: e.target.value, page: 0 })}
                className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-900 shadow-sm outline-none transition focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
              >
                <option value="">All</option>
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="RESERVED">RESERVED</option>
                <option value="SOLD">SOLD</option>
              </select>
            </label>
            <Button variant="secondary" className="gap-2" onClick={() => setParam({ category, status, page: 0 })}>
              <Filter className="h-4 w-4" /> Apply
            </Button>
            <Button variant="ghost" onClick={() => setParam({ category: '', status: '', page: 0 })}>
              Clear
            </Button>
          </div>
        </div>

        {err ? <Alert tone="error" title="Could not load marketplace">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading marketplace" />
        ) : items?.length ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {items.map((item) => (
              <MarketplaceCard key={item.id} item={item} />
            ))}
          </div>
        ) : (
          <EmptyState
            title="No marketplace items found"
            hint={category || status ? 'Try adjusting the filters.' : 'Be the first to list something useful for campus life.'}
            icon={PackageSearch}
          />
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
