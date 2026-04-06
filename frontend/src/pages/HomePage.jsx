import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowRight, FileQuestion, PlusCircle, ShieldCheck, ShoppingBag, Sparkles } from 'lucide-react'
import { listQuestions } from '../api/questionApi'
import { listLostFound } from '../api/lostFoundApi'
import { getMarketplaceItems } from '../api/marketplaceApi'
import { getApiErrorMessage } from '../api/axios'
import { PageContainer } from '../components/layout/PageContainer'
import { Button } from '../components/common/Button'
import { Loader } from '../components/common/Loader'
import { Alert } from '../components/common/Alert'
import { QuestionCard } from '../components/qa/QuestionCard'
import { LostFoundCard } from '../components/lostfound/LostFoundCard'
import { MarketplaceCard } from '../components/marketplace/MarketplaceCard'
import { useAuth } from '../hooks/useAuth'

function ActionCard({ to, icon: Icon, title, desc }) {
  return (
    <Link to={to} className="cc-card cc-card-hover group block p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-600 to-blue-600 shadow-sm">
            <Icon className="h-5 w-5 text-white" />
          </div>
          <div className="mt-3 text-base font-extrabold tracking-tight text-slate-900">{title}</div>
          <div className="mt-1 text-sm text-slate-600">{desc}</div>
        </div>
        <ArrowRight className="mt-1 h-5 w-5 text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-slate-600" />
      </div>
    </Link>
  )
}

export function HomePage() {
  const { user, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [qLoading, setQLoading] = useState(true)
  const [lfLoading, setLfLoading] = useState(true)
  const [marketLoading, setMarketLoading] = useState(true)
  const [err, setErr] = useState('')
  const [questions, setQuestions] = useState([])
  const [posts, setPosts] = useState([])
  const [marketItems, setMarketItems] = useState([])

  useEffect(() => {
    let alive = true
    ;(async () => {
      setErr('')
      try {
        const [qs, lfs, marketplace] = await Promise.all([
          listQuestions({ page: 0, size: 4 }),
          listLostFound({ page: 0, size: 4 }),
          getMarketplaceItems({ page: 0, size: 4 }),
        ])
        if (alive) {
          setQuestions(qs || [])
          setPosts(lfs || [])
          setMarketItems(marketplace || [])
        }
      } catch (e) {
        if (alive) setErr(getApiErrorMessage(e))
      } finally {
        if (alive) {
          setQLoading(false)
          setLfLoading(false)
          setMarketLoading(false)
        }
      }
    })()
    return () => {
      alive = false
    }
  }, [])

  return (
    <PageContainer
      title={`Hi ${user?.fullName?.split(' ')?.[0] || 'there'}`}
      subtitle="Your campus dashboard. Jump into Q&A, report a lost item, or reserve something useful from the marketplace."
      actions={
        <Button variant="secondary" onClick={() => navigate('/marketplace')} className="gap-2">
          <ShoppingBag className="h-4 w-4" /> Browse Marketplace
        </Button>
      }
    >
      <div className="grid gap-6">
        <div className="cc-card overflow-hidden">
          <div className="relative p-7 sm:p-9">
            <div className="absolute -right-20 -top-16 h-56 w-56 rounded-full bg-indigo-200/50 blur-3xl" />
            <div className="absolute -bottom-20 -left-24 h-64 w-64 rounded-full bg-blue-200/40 blur-3xl" />
            <div className="relative grid gap-6 lg:grid-cols-3">
              <div className="lg:col-span-2">
                <div className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white/70 px-3 py-2 text-xs font-extrabold text-slate-700">
                  <ShieldCheck className="h-4 w-4 text-indigo-700" />
                  JWT auth · Role-based access · Marketplace reservations · Clean architecture
                </div>
                <div className="mt-4 text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl">
                  Ask better. <span className="text-indigo-700">Recover faster.</span> Buy smarter on campus.
                </div>
                <div className="mt-3 max-w-2xl text-sm leading-relaxed text-slate-600">
                  CampusConnect now brings together Q&A, Lost & Found, and a token-based campus marketplace with backend-verified Razorpay test payments.
                </div>
              </div>
              <div className="grid gap-3">
                <div className="cc-card bg-white/60 p-4">
                  <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                    <Sparkles className="h-4 w-4 text-indigo-700" />
                    Reputation
                  </div>
                  <div className="mt-1 text-sm font-semibold text-slate-600">
                    {user?.reputationPoints ?? 0} points
                  </div>
                </div>
                <div className="cc-card bg-white/60 p-4">
                  <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                    <FileQuestion className="h-4 w-4 text-indigo-700" />
                    Quick actions
                  </div>
                  <div className="mt-2 flex flex-wrap gap-2">
                    <Button size="sm" onClick={() => navigate('/ask')} className="gap-1.5">
                      <PlusCircle className="h-4 w-4" /> Ask
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => navigate('/lost-found/new')} className="gap-1.5">
                      <PlusCircle className="h-4 w-4" /> Report
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => navigate('/marketplace/create')} className="gap-1.5">
                      <PlusCircle className="h-4 w-4" /> Sell
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {err ? <Alert tone="error" title="Could not load dashboard">{err}</Alert> : null}

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <ActionCard to="/ask" icon={FileQuestion} title="Ask a Question" desc="Get help fast with clean, tagged questions." />
          <ActionCard to="/lost-found/new" icon={PlusCircle} title="Report Lost/Found" desc="Post location and incident date with optional image URL." />
          <ActionCard to="/marketplace/create" icon={ShoppingBag} title="Create Listing" desc="Sell a campus item and accept token reservations online." />
          {isAdmin ? (
            <ActionCard to="/admin" icon={ShieldCheck} title="Admin Portal" desc="Review platform activity and moderate content." />
          ) : null}
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          <div>
            <div className="mb-4 flex items-end justify-between gap-3">
              <div>
                <div className="text-lg font-extrabold text-slate-900">Latest Questions</div>
                <div className="text-sm text-slate-600">Fresh discussions from the community.</div>
              </div>
              <Button variant="ghost" size="sm" onClick={() => navigate('/questions')} className="gap-1.5">
                View all <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
            {qLoading ? (
              <Loader label="Loading questions" />
            ) : (
              <div className="grid gap-3">
                {questions.map((q) => (
                  <QuestionCard key={q.id} q={q} />
                ))}
              </div>
            )}
          </div>

          <div>
            <div className="mb-4 flex items-end justify-between gap-3">
              <div>
                <div className="text-lg font-extrabold text-slate-900">Latest Lost & Found</div>
                <div className="text-sm text-slate-600">Recent reports across campus.</div>
              </div>
              <Button variant="ghost" size="sm" onClick={() => navigate('/lost-found')} className="gap-1.5">
                View all <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
            {lfLoading ? (
              <Loader label="Loading posts" />
            ) : (
              <div className="grid gap-3 sm:grid-cols-2">
                {posts.map((p) => (
                  <LostFoundCard key={p.id} post={p} />
                ))}
              </div>
            )}
          </div>
        </div>

        <div>
          <div className="mb-4 flex items-end justify-between gap-3">
            <div>
              <div className="text-lg font-extrabold text-slate-900">Latest Marketplace Listings</div>
              <div className="text-sm text-slate-600">Reserve items with a small token payment.</div>
            </div>
            <Button variant="ghost" size="sm" onClick={() => navigate('/marketplace')} className="gap-1.5">
              View all <ArrowRight className="h-4 w-4" />
            </Button>
          </div>
          {marketLoading ? (
            <Loader label="Loading marketplace" />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              {marketItems.map((item) => (
                <MarketplaceCard key={item.id} item={item} />
              ))}
            </div>
          )}
        </div>
      </div>
    </PageContainer>
  )
}
