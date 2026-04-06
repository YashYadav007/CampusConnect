import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { Copy, CreditCard, Mail, PackageCheck, Store, User } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Loader } from '../components/common/Loader'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { Button } from '../components/common/Button'
import { formatDateTime } from '../utils/formatDate'
import { getApiErrorMessage } from '../api/axios'
import {
  createMarketplaceOrder,
  getMarketplaceItemById,
  markMarketplaceItemSold,
  verifyMarketplacePayment,
} from '../api/marketplaceApi'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { toneForMarketplaceStatus } from '../components/marketplace/marketplaceBadges'
import { loadRazorpayCheckout } from '../utils/loadRazorpayCheckout'

function formatPrice(value) {
  const numeric = Number(value || 0)
  return `₹${numeric.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function MarketplaceDetailPage() {
  const { id } = useParams()
  const { isAuthed, user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()

  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [item, setItem] = useState(null)
  const [paying, setPaying] = useState(false)
  const [markingSold, setMarkingSold] = useState(false)

  const isOwner = Boolean(isAuthed && user?.id && item?.seller?.id && user.id === item.seller.id)
  const canReserve = Boolean(isAuthed && !isOwner && item?.status === 'AVAILABLE')
  const canMarkSold = Boolean(isOwner && item?.status !== 'SOLD')

  const reservationText = useMemo(() => {
    if (!item?.reservedBy) return null
    if (user?.id && item.reservedBy.id === user.id) return 'Reserved by you'
    return `Reserved by ${item.reservedBy.fullName}`
  }, [item?.reservedBy, user?.id])

  const loadItem = async () => {
    const data = await getMarketplaceItemById(id)
    setItem(data)
    return data
  }

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await getMarketplaceItemById(id)
        if (alive) setItem(data)
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

  const openCheckout = async () => {
    if (!isAuthed) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }

    setErr('')
    setPaying(true)
    try {
      await loadRazorpayCheckout()
      const order = await createMarketplaceOrder(item.id)

      const razorpay = new window.Razorpay({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        order_id: order.orderId,
        name: 'CampusConnect Marketplace',
        description: `Reserve ${order.item.title}`,
        prefill: {
          name: user?.fullName,
          email: user?.email,
        },
        modal: {
          ondismiss: () => {
            setPaying(false)
          },
        },
        handler: async (response) => {
          try {
            await verifyMarketplacePayment({
              itemId: item.id,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            showToast({
              tone: 'success',
              title: 'Reservation confirmed',
              message: 'Payment verified on the backend and the item is now RESERVED.',
            })
            await loadItem()
          } catch (e) {
            const message = getApiErrorMessage(e)
            setErr(message)
            showToast({ tone: 'error', title: 'Verification failed', message })
          } finally {
            setPaying(false)
          }
        },
      })

      razorpay.on('payment.failed', (response) => {
        const message = response?.error?.description || 'Payment failed before verification'
        setErr(message)
        showToast({ tone: 'error', title: 'Payment failed', message })
        setPaying(false)
      })

      razorpay.open()
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Could not start checkout', message })
      setPaying(false)
    }
  }

  const onMarkSold = async () => {
    setMarkingSold(true)
    setErr('')
    try {
      await markMarketplaceItemSold(item.id)
      await loadItem()
      showToast({ tone: 'success', title: 'Listing updated', message: 'Item marked as SOLD.' })
    } catch (e) {
      const message = getApiErrorMessage(e)
      setErr(message)
      showToast({ tone: 'error', title: 'Could not mark sold', message })
    } finally {
      setMarkingSold(false)
    }
  }

  const copySellerEmail = async () => {
    if (!item?.sellerEmail) return
    try {
      await navigator.clipboard.writeText(item.sellerEmail)
      showToast({ tone: 'success', title: 'Email copied', message: 'Seller email copied to clipboard.' })
    } catch {
      showToast({ tone: 'error', title: 'Copy failed', message: 'Could not copy email to clipboard.' })
    }
  }

  if (loading) {
    return (
      <PageContainer title="Marketplace" subtitle="Loading item">
        <Loader label="Loading marketplace item" />
      </PageContainer>
    )
  }

  if (err && !item) {
    return (
      <PageContainer title="Marketplace" subtitle="Could not load item">
        <Alert tone="error" title="Error">{err}</Alert>
      </PageContainer>
    )
  }

  if (!item) return null

  return (
    <PageContainer
      title={item.title}
      subtitle={`Listed by ${item.seller?.fullName || 'Unknown'} · ${formatDateTime(item.createdAt)}`}
      actions={<Badge tone={toneForMarketplaceStatus(item.status)}>{item.status}</Badge>}
    >
      <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <div className="cc-card overflow-hidden">
          {item.imageUrl ? (
            <div className="h-80 overflow-hidden bg-slate-100">
              <img src={item.imageUrl} alt={item.title} className="h-full w-full object-cover" />
            </div>
          ) : (
            <div className="flex h-80 items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
              <div className="text-sm font-bold text-slate-600">No image provided</div>
            </div>
          )}

          <div className="grid gap-5 p-6 sm:p-7">
            {err ? <Alert tone="error" title="Something went wrong">{err}</Alert> : null}

            <div>
              <div className="flex flex-wrap items-center gap-2 text-sm font-semibold text-slate-500">
                <span>{item.category}</span>
                <span>•</span>
                <span>{item.conditionLabel}</span>
              </div>
              {item.description ? (
                <div className="mt-4 whitespace-pre-wrap text-sm leading-relaxed text-slate-700">{item.description}</div>
              ) : (
                <div className="mt-4 text-sm text-slate-500">No description provided.</div>
              )}
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="text-sm font-extrabold text-slate-900">Full Price</div>
                <div className="mt-1 text-2xl font-extrabold tracking-tight text-slate-900">{formatPrice(item.price)}</div>
              </div>
              <div className="rounded-2xl border border-indigo-200 bg-indigo-50 p-4">
                <div className="text-sm font-extrabold text-indigo-900">Reserve Token</div>
                <div className="mt-1 text-2xl font-extrabold tracking-tight text-indigo-900">{formatPrice(item.tokenAmount)}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-4">
          <div className="cc-card p-5">
            <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
              <Store className="h-4 w-4 text-indigo-700" /> Seller
            </div>
            <div className="mt-2 text-sm font-semibold text-slate-700">{item.seller?.fullName || 'Unknown'}</div>
          </div>

          <div className="cc-card p-5">
            <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
              <Mail className="h-4 w-4 text-indigo-700" /> Contact Seller
            </div>
            {isOwner ? (
              <div className="mt-2 text-sm font-semibold text-slate-600">This is your listing.</div>
            ) : (
              <div className="mt-3 grid gap-3">
                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 break-all">
                  {item.sellerEmail || 'Email not available'}
                </div>
                <Button
                  variant="secondary"
                  className="gap-2"
                  onClick={copySellerEmail}
                  disabled={!item.sellerEmail}
                >
                  <Copy className="h-4 w-4" /> Copy Email
                </Button>
              </div>
            )}
          </div>

          <div className="cc-card p-5">
            <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
              <PackageCheck className="h-4 w-4 text-indigo-700" /> Listing Status
            </div>
            <div className="mt-3 flex items-center gap-2">
              <Badge tone={toneForMarketplaceStatus(item.status)}>{item.status}</Badge>
              {reservationText ? <span className="text-sm font-semibold text-slate-600">{reservationText}</span> : null}
            </div>
          </div>

          <div className="cc-card p-5">
            <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
              <User className="h-4 w-4 text-indigo-700" /> Reservation Flow
            </div>
            <div className="mt-2 text-sm leading-relaxed text-slate-600">
              Buyers pay a small token online in Razorpay test mode. The listing becomes RESERVED only after backend signature verification succeeds.
            </div>

            <div className="mt-4 grid gap-3">
              {canReserve ? (
                <Button className="gap-2" loading={paying} disabled={paying} onClick={openCheckout}>
                  <CreditCard className="h-4 w-4" /> Reserve with Token Payment
                </Button>
              ) : null}

              {!isAuthed && item.status === 'AVAILABLE' ? (
                <Button variant="secondary" onClick={() => navigate('/login', { state: { from: location.pathname } })}>
                  Login to Reserve
                </Button>
              ) : null}

              {canMarkSold ? (
                <Button variant="secondary" loading={markingSold} disabled={markingSold} onClick={onMarkSold}>
                  Mark as Sold
                </Button>
              ) : null}

              {isOwner ? (
                <div className="text-xs font-semibold text-slate-500">
                  You are the seller for this listing. Buyers cannot reserve until backend verification completes.
                </div>
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </PageContainer>
  )
}
