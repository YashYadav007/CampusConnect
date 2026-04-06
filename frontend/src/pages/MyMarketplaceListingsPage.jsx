import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageContainer } from '../components/layout/PageContainer'
import { Loader } from '../components/common/Loader'
import { Alert } from '../components/common/Alert'
import { EmptyState } from '../components/common/EmptyState'
import { Button } from '../components/common/Button'
import { MarketplaceCard } from '../components/marketplace/MarketplaceCard'
import { getMyMarketplaceListings, markMarketplaceItemSold } from '../api/marketplaceApi'
import { getApiErrorMessage } from '../api/axios'
import { useToast } from '../hooks/useToast'

export function MyMarketplaceListingsPage() {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [items, setItems] = useState([])
  const [actingId, setActingId] = useState(null)

  const loadItems = async () => {
    const data = await getMyMarketplaceListings()
    setItems(data || [])
  }

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await getMyMarketplaceListings()
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
  }, [])

  const onMarkSold = async (id) => {
    setActingId(id)
    setErr('')
    try {
      await markMarketplaceItemSold(id)
      showToast({ tone: 'success', title: 'Listing updated', message: 'Your item is now marked SOLD.' })
      await loadItems()
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Could not update listing', message })
    } finally {
      setActingId(null)
    }
  }

  return (
    <PageContainer
      title="My Marketplace Listings"
      subtitle="Manage your active campus listings and mark items sold when the handoff is complete."
      actions={<Button onClick={() => navigate('/marketplace/create')}>Create Listing</Button>}
    >
      <div className="grid gap-5">
        {err ? <Alert tone="error" title="Could not load listings">{err}</Alert> : null}

        {loading ? (
          <Loader label="Loading your listings" />
        ) : items?.length ? (
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
            {items.map((item) => (
              <div key={item.id} className="grid gap-3">
                <MarketplaceCard item={item} />
                <div className="cc-card flex items-center justify-between p-4">
                  <div className="text-sm font-semibold text-slate-600">
                    {item.status === 'SOLD'
                      ? 'This listing is closed.'
                      : item.status === 'RESERVED'
                        ? 'Buyer has reserved this item.'
                        : 'Available for reservation.'}
                  </div>
                  {item.status !== 'SOLD' ? (
                    <Button
                      size="sm"
                      variant="secondary"
                      loading={actingId === item.id}
                      disabled={actingId === item.id}
                      onClick={() => onMarkSold(item.id)}
                    >
                      Mark Sold
                    </Button>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState title="No listings yet" hint="Create your first marketplace listing to start selling on campus." />
        )}
      </div>
    </PageContainer>
  )
}
