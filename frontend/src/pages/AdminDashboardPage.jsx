import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, ClipboardList, MessageCircleQuestion, PackageSearch, Users } from 'lucide-react'
import { getAdminStats } from '../api/adminApi'
import { getApiErrorMessage } from '../api/axios'
import { AdminLayout } from '../components/admin/AdminLayout'
import { Alert } from '../components/common/Alert'
import { Loader } from '../components/common/Loader'

function StatCard({ label, value, hint }) {
  return (
    <div className="cc-card cc-card-hover p-5">
      <div className="text-sm font-semibold text-slate-500">{label}</div>
      <div className="mt-2 text-3xl font-extrabold tracking-tight text-slate-900">{value}</div>
      <div className="mt-2 text-sm text-slate-600">{hint}</div>
    </div>
  )
}

function QuickLink({ to, icon: Icon, title, hint }) {
  return (
    <Link to={to} className="cc-card cc-card-hover flex items-center justify-between gap-4 p-4">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-600 to-blue-600 text-white shadow-sm">
          <Icon className="h-5 w-5" />
        </div>
        <div>
          <div className="text-sm font-extrabold text-slate-900">{title}</div>
          <div className="text-sm text-slate-600">{hint}</div>
        </div>
      </div>
      <ArrowRight className="h-4 w-4 text-slate-400" />
    </Link>
  )
}

export function AdminDashboardPage() {
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [stats, setStats] = useState(null)

  useEffect(() => {
    let alive = true
    ;(async () => {
      setLoading(true)
      setErr('')
      try {
        const data = await getAdminStats()
        if (alive) setStats(data)
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

  return (
    <AdminLayout title="Admin Dashboard" subtitle="Platform stats and moderation shortcuts.">
      {err ? <Alert tone="error" title="Could not load admin stats">{err}</Alert> : null}

      {loading ? (
        <Loader label="Loading dashboard" />
      ) : stats ? (
        <div className="grid gap-6">
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard label="Total Users" value={stats.totalUsers} hint={`${stats.activeUsers} active accounts`} />
            <StatCard label="Questions" value={stats.totalQuestions} hint={`${stats.totalAnswers} answers posted`} />
            <StatCard label="Lost & Found" value={stats.totalLostFoundPosts} hint={`${stats.openLostFoundPosts} posts still open`} />
            <StatCard label="Claims" value={stats.totalClaims} hint={`${stats.pendingClaims} pending review`} />
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <QuickLink to="/admin/users" icon={Users} title="Manage Users" hint="Review roles, status, and reputation." />
            <QuickLink to="/admin/questions" icon={MessageCircleQuestion} title="Moderate Questions" hint="Inspect threads and remove content safely." />
            <QuickLink to="/admin/lost-found" icon={PackageSearch} title="Moderate Lost & Found" hint="Review posts and remove resolved or invalid items." />
            <QuickLink to="/admin/claims" icon={ClipboardList} title="Review Claims" hint="See claim activity across the platform." />
          </div>
        </div>
      ) : null}
    </AdminLayout>
  )
}
