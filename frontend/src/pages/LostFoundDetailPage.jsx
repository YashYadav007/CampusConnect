import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { CalendarDays, Copy, Mail, MapPin, ShieldCheck, User } from 'lucide-react'
import { PageContainer } from '../components/layout/PageContainer'
import { Loader } from '../components/common/Loader'
import { Alert } from '../components/common/Alert'
import { Badge } from '../components/common/Badge'
import { Button } from '../components/common/Button'
import { Modal } from '../components/common/Modal'
import { Textarea } from '../components/common/Textarea'
import { formatDate, formatDateTime } from '../utils/formatDate'
import { getApiErrorMessage } from '../api/axios'
import { getLostFound } from '../api/lostFoundApi'
import { approveClaim, createClaim, listClaimsForPost, rejectClaim } from '../api/claimApi'
import { EmptyState } from '../components/common/EmptyState'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { toneForItemStatus, toneForPostType } from '../components/lostfound/lostFoundBadges'

function toneForClaimStatus(status) {
  if (status === 'PENDING') return 'amber'
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'slate'
}

export function LostFoundDetailPage() {
  const { id } = useParams()
  const { isAuthed, user } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [post, setPost] = useState(null)
  const [claims, setClaims] = useState([])
  const [claimsLoading, setClaimsLoading] = useState(false)
  const [claimModalOpen, setClaimModalOpen] = useState(false)
  const [claimMessage, setClaimMessage] = useState('')
  const [claimSubmitting, setClaimSubmitting] = useState(false)
  const [claimSubmitted, setClaimSubmitted] = useState(false)
  const [claimActingId, setClaimActingId] = useState(null)

  const isOwner = Boolean(isAuthed && user?.id && post?.user?.id && user.id === post.user.id)
  const isClaimable =
    Boolean(isAuthed) &&
    post?.type === 'FOUND' &&
    post?.status === 'OPEN' &&
    !isOwner

  const showLoginToClaim =
    !isAuthed &&
    post?.type === 'FOUND' &&
    post?.status === 'OPEN'

  const refreshPost = async () => {
    const data = await getLostFound(id)
    setPost(data)
    return data
  }

  const refreshClaims = async (p) => {
    const postToUse = p || post
    if (!postToUse || !isOwner || postToUse.type !== 'FOUND') return
    setClaimsLoading(true)
    try {
      const data = await listClaimsForPost(postToUse.id)
      setClaims(data || [])
    } finally {
      setClaimsLoading(false)
    }
  }

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      setClaimSubmitted(false)
      try {
        const data = await getLostFound(id)
        if (!alive) return
        setPost(data)
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

  useEffect(() => {
    if (!post) return
    setErr('')
    if (isOwner && post.type === 'FOUND') {
      refreshClaims(post).catch((e) => setErr(getApiErrorMessage(e)))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [post?.id, isOwner])

  const submitClaim = async () => {
    if (!isAuthed) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }
    const msg = claimMessage.trim()
    if (msg.length < 10) {
      setErr('Claim message must be at least 10 characters')
      return
    }
    setErr('')
    setClaimSubmitting(true)
    try {
      await createClaim(post.id, { message: msg })
      setClaimSubmitted(true)
      setClaimModalOpen(false)
      setClaimMessage('')
      showToast({ tone: 'success', title: 'Claim submitted', message: 'Your claim request was sent to the post owner.' })
      await refreshPost()
    } catch (e) {
      const m = getApiErrorMessage(e)
      setErr(m)
      showToast({ tone: 'error', title: 'Claim failed', message: m })
    } finally {
      setClaimSubmitting(false)
    }
  }

  const actOnClaim = async (claimId, action) => {
    setErr('')
    setClaimActingId(claimId)
    try {
      if (action === 'approve') {
        await approveClaim(claimId)
        showToast({ tone: 'success', title: 'Claim approved', message: 'Post marked as RESOLVED and other claims rejected.' })
      } else {
        await rejectClaim(claimId)
        showToast({ tone: 'success', title: 'Claim rejected', message: 'The claim request was rejected.' })
      }
      const p = await refreshPost()
      await refreshClaims(p)
    } catch (e) {
      const m = getApiErrorMessage(e)
      setErr(m)
      showToast({ tone: 'error', title: 'Action failed', message: m })
    } finally {
      setClaimActingId(null)
    }
  }

  const copyOwnerEmail = async () => {
    if (!post?.ownerEmail) return
    try {
      await navigator.clipboard.writeText(post.ownerEmail)
      showToast({ tone: 'success', title: 'Email copied', message: 'Owner email copied to clipboard.' })
    } catch {
      showToast({ tone: 'error', title: 'Copy failed', message: 'Could not copy email to clipboard.' })
    }
  }

  if (loading) {
    return (
      <PageContainer title="Lost & Found" subtitle="Loading post">
        <Loader label="Loading post" />
      </PageContainer>
    )
  }

  if (err && !post) {
    return (
      <PageContainer title="Lost & Found" subtitle="Could not load post">
        <Alert tone="error" title="Error">{err}</Alert>
      </PageContainer>
    )
  }

  if (!post) return null

  return (
    <PageContainer title={post.title} subtitle={`Posted by ${post.user?.fullName || 'Unknown'} · ${formatDateTime(post.createdAt)}`}>
      <div className="grid gap-6">
        <div className="cc-card overflow-hidden">
          {post.imageUrl ? (
            <div className="relative h-72 w-full bg-slate-100">
              <img src={post.imageUrl} alt={post.title} className="h-full w-full object-cover" />
              <div className="absolute left-5 top-5 flex items-center gap-2">
                <Badge tone={toneForPostType(post.type)}>{post.type}</Badge>
                <Badge tone={toneForItemStatus(post.status)}>{post.status}</Badge>
              </div>
            </div>
          ) : (
            <div className="relative flex h-56 items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
              <div className="absolute left-5 top-5 flex items-center gap-2">
                <Badge tone={toneForPostType(post.type)}>{post.type}</Badge>
                <Badge tone={toneForItemStatus(post.status)}>{post.status}</Badge>
              </div>
              <div className="text-sm font-bold text-slate-600">No image provided</div>
            </div>
          )}

          <div className="p-6 sm:p-7">
            {err ? <Alert tone="error" title="Something went wrong">{err}</Alert> : null}

            {post.description ? (
              <div className="whitespace-pre-wrap text-sm leading-relaxed text-slate-800">{post.description}</div>
            ) : (
              <div className="text-sm text-slate-600">No description provided.</div>
            )}

            <div className="mt-5 grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                  <MapPin className="h-4 w-4 text-indigo-700" />
                  Location
                </div>
                <div className="mt-1 text-sm font-semibold text-slate-700">{post.location}</div>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                  <CalendarDays className="h-4 w-4 text-indigo-700" />
                  Incident
                </div>
                <div className="mt-1 text-sm font-semibold text-slate-700">{formatDate(post.dateOfIncident)}</div>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                  <User className="h-4 w-4 text-indigo-700" />
                  Author
                </div>
                <div className="mt-1 text-sm font-semibold text-slate-700">{post.user?.fullName || 'Unknown'}</div>
              </div>
            </div>

            <div className="mt-5 rounded-2xl border border-slate-200 bg-white p-4">
              <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                <Mail className="h-4 w-4 text-indigo-700" />
                Contact Owner
              </div>
              {isOwner ? (
                <div className="mt-2 text-sm font-semibold text-slate-600">This is your post.</div>
              ) : (
                <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 break-all">
                    {post.ownerEmail || 'Email not available'}
                  </div>
                  <Button
                    variant="secondary"
                    className="gap-2"
                    onClick={copyOwnerEmail}
                    disabled={!post.ownerEmail}
                  >
                    <Copy className="h-4 w-4" /> Copy Email
                  </Button>
                </div>
              )}
            </div>

            {showLoginToClaim ? (
              <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="inline-flex items-start gap-2 rounded-2xl border border-slate-200 bg-white/70 px-4 py-3 text-sm text-slate-700">
                  <ShieldCheck className="mt-0.5 h-5 w-5 text-indigo-700" />
                  <div>
                    <div className="font-extrabold text-slate-900">Claim this item</div>
                    <div className="text-slate-600">Login to submit a claim request to the post owner.</div>
                  </div>
                </div>
                <Button onClick={() => navigate('/login', { state: { from: location.pathname } })} className="w-full sm:w-auto">
                  Login to claim
                </Button>
              </div>
            ) : null}

            {isClaimable ? (
              <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="inline-flex items-start gap-2 rounded-2xl border border-slate-200 bg-white/70 px-4 py-3 text-sm text-slate-700">
                  <ShieldCheck className="mt-0.5 h-5 w-5 text-indigo-700" />
                  <div>
                    <div className="font-extrabold text-slate-900">Claim this item</div>
                    <div className="text-slate-600">Provide details to prove ownership. The post owner will review.</div>
                  </div>
                </div>
                <Button
                  onClick={() => setClaimModalOpen(true)}
                  disabled={claimSubmitted}
                  className="w-full sm:w-auto"
                >
                  {claimSubmitted ? 'Claim submitted' : 'Claim this item'}
                </Button>
              </div>
            ) : null}
          </div>
        </div>

        {isOwner && post.type === 'FOUND' ? (
          <div className="cc-card p-6">
            <div className="flex items-end justify-between gap-3">
              <div>
                <div className="text-lg font-extrabold text-slate-900">Claim Requests</div>
                <div className="text-sm text-slate-600">Approve one claim to resolve this found item.</div>
              </div>
              <Badge tone={toneForItemStatus(post.status)}>{post.status}</Badge>
            </div>

            {claimsLoading ? (
              <Loader label="Loading claims" />
            ) : claims?.length ? (
              <div className="mt-4 grid gap-3">
                {claims.map((c) => (
                  <div key={c.id} className="cc-card p-5">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <div className="text-sm font-extrabold text-slate-900">{c.claimer?.fullName || 'Unknown'}</div>
                          <Badge tone={toneForClaimStatus(c.status)}>{c.status}</Badge>
                        </div>
                        <div className="mt-2 whitespace-pre-wrap text-sm text-slate-700">{c.message}</div>
                        <div className="mt-3 text-xs font-semibold text-slate-500">{formatDateTime(c.createdAt)}</div>
                      </div>

                      {c.status === 'PENDING' && post.status === 'OPEN' ? (
                        <div className="flex shrink-0 items-center gap-2">
                          <Button
                            size="sm"
                            loading={claimActingId === c.id}
                            onClick={() => actOnClaim(c.id, 'approve')}
                            className="w-full sm:w-auto"
                          >
                            Approve
                          </Button>
                          <Button
                            size="sm"
                            variant="secondary"
                            disabled={claimActingId === c.id}
                            onClick={() => actOnClaim(c.id, 'reject')}
                            className="w-full sm:w-auto"
                          >
                            Reject
                          </Button>
                        </div>
                      ) : null}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="mt-4">
                <EmptyState title="No claims yet" hint="When someone claims this item, requests will appear here." />
              </div>
            )}
          </div>
        ) : null}
      </div>

      <Modal
        open={claimModalOpen}
        title="Claim this item"
        subtitle="Provide details to prove ownership."
        onClose={() => {
          if (claimSubmitting) return
          setClaimModalOpen(false)
        }}
        footer={
          <>
            <Button variant="ghost" onClick={() => setClaimModalOpen(false)} disabled={claimSubmitting}>
              Cancel
            </Button>
            <Button onClick={submitClaim} loading={claimSubmitting}>
              Submit claim
            </Button>
          </>
        }
      >
        <div className="grid gap-3">
          <div className="text-sm text-slate-600">
            Include identifiers like approximate contents, markings, colors, or anything only the real owner would know.
          </div>
          <Textarea
            rows={6}
            label="Message"
            placeholder="Example: This wallet has my student ID card (ending 0421) and a SBI debit card..."
            value={claimMessage}
            onChange={(e) => setClaimMessage(e.target.value)}
            disabled={claimSubmitting}
          />
          <div className="text-xs font-semibold text-slate-500">Minimum 10 characters.</div>
        </div>
      </Modal>
    </PageContainer>
  )
}
