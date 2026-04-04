import { NavLink } from 'react-router-dom'
import { ClipboardList, LayoutDashboard, MessageCircleQuestion, ShieldCheck, Users, PackageSearch } from 'lucide-react'
import clsx from 'clsx'
import { PageContainer } from '../layout/PageContainer'
import { Badge } from '../common/Badge'
import { useAuth } from '../../hooks/useAuth'

const items = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/questions', label: 'Questions', icon: MessageCircleQuestion },
  { to: '/admin/lost-found', label: 'Lost & Found', icon: PackageSearch },
  { to: '/admin/claims', label: 'Claims', icon: ClipboardList },
]

function AdminNavItem({ to, label, icon: Icon, end }) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        clsx(
          'flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-semibold transition',
          isActive
            ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-sm'
            : 'text-slate-700 hover:bg-slate-100 hover:text-slate-900'
        )
      }
    >
      <Icon className="h-4 w-4" />
      <span>{label}</span>
    </NavLink>
  )
}

export function AdminLayout({ title, subtitle, actions, children }) {
  const { user } = useAuth()

  return (
    <PageContainer title={title} subtitle={subtitle} actions={actions}>
      <div className="grid gap-6 lg:grid-cols-[260px_minmax(0,1fr)]">
        <aside className="cc-card h-fit p-4 lg:sticky lg:top-24">
          <div className="flex items-start justify-between gap-3 border-b border-slate-200/70 pb-4">
            <div>
              <div className="flex items-center gap-2 text-sm font-extrabold text-slate-900">
                <ShieldCheck className="h-4 w-4 text-indigo-700" />
                Admin Portal
              </div>
              <div className="mt-1 text-xs font-semibold text-slate-500">Moderation and platform overview</div>
            </div>
            <Badge tone="indigo">ADMIN</Badge>
          </div>

          <div className="mt-4 grid gap-2">
            {items.map((item) => (
              <AdminNavItem key={item.to} {...item} />
            ))}
          </div>

          <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-3">
            <div className="text-xs font-semibold text-slate-500">Signed in as</div>
            <div className="mt-1 text-sm font-extrabold text-slate-900">{user?.fullName}</div>
            <div className="text-xs font-semibold text-slate-500">{user?.email}</div>
          </div>
        </aside>

        <div className="min-w-0">{children}</div>
      </div>
    </PageContainer>
  )
}
