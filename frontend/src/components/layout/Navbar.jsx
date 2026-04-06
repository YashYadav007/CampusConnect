import { NavLink, Link, useLocation, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { LogOut, Menu, MessageCircleQuestion, Search, Shapes, ShieldCheck, ShoppingBag, X } from 'lucide-react'
import clsx from 'clsx'
import { useAuth } from '../../hooks/useAuth'
import { Button } from '../common/Button'
import { useEffect, useRef, useState } from 'react'
import { Badge } from '../common/Badge'

function NavItem({ to, icon: Icon, label }) {
  const MotionSpan = motion.span
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        clsx(
          'relative inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-semibold transition',
          isActive ? 'text-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
        )
      }
    >
      {({ isActive }) => (
        <>
          <Icon className="h-4 w-4" />
          <span>{label}</span>
          {isActive ? (
            <MotionSpan
              layoutId="nav-underline"
              className="absolute inset-x-2 -bottom-1 h-0.5 rounded-full bg-gradient-to-r from-indigo-600 to-blue-600"
            />
          ) : null}
        </>
      )}
    </NavLink>
  )
}

export function Navbar() {
  const { isAuthed, isAdmin, user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const MotionDiv = motion.div
  const location = useLocation()
  const menuRef = useRef(null)

  const onLogout = () => {
    logout()
    navigate('/login')
  }

  useEffect(() => {
    setOpen(false)
    setUserMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    const onDown = (e) => {
      if (!userMenuOpen) return
      const el = menuRef.current
      if (!el) return
      if (!el.contains(e.target)) {
        setUserMenuOpen(false)
      }
    }
    window.addEventListener('mousedown', onDown)
    return () => window.removeEventListener('mousedown', onDown)
  }, [userMenuOpen])

  const initials = (user?.fullName || 'U')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('')

  return (
    <div className="sticky top-0 z-50 border-b border-slate-200/70 bg-white/70 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
        <Link to={isAuthed ? '/' : '/questions'} className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-600 to-blue-600 shadow-sm">
            <Shapes className="h-5 w-5 text-white" />
          </div>
          <div className="leading-tight">
            <div className="text-sm font-extrabold tracking-tight text-slate-900">CampusConnect</div>
            <div className="text-[11px] font-semibold text-slate-500">Student Portal</div>
          </div>
        </Link>

        <div className="hidden items-center gap-1 md:flex">
          {isAuthed ? <NavItem to="/" icon={Search} label="Home" /> : null}
          <NavItem to="/questions" icon={MessageCircleQuestion} label="Q&A" />
          <NavItem to="/lost-found" icon={Shapes} label="Lost & Found" />
          <NavItem to="/marketplace" icon={ShoppingBag} label="Marketplace" />
          {isAdmin ? <NavItem to="/admin" icon={ShieldCheck} label="Admin" /> : null}
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            className="md:hidden"
            onClick={() => setOpen((v) => !v)}
            aria-label={open ? 'Close menu' : 'Open menu'}
          >
            {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
          {isAuthed ? (
            <div className="relative" ref={menuRef}>
              <button
                type="button"
                onClick={() => setUserMenuOpen((v) => !v)}
                className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white px-2.5 py-2 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-slate-50"
                aria-label="User menu"
              >
                <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-600 to-blue-600 text-xs font-extrabold text-white">
                  {initials}
                </div>
                <div className="hidden text-left sm:block">
                  <div className="flex items-center gap-2 text-sm font-extrabold leading-tight">
                    <span>{user?.fullName}</span>
                    {isAdmin ? <Badge tone="indigo">ADMIN</Badge> : null}
                  </div>
                  <div className="text-[11px] font-semibold text-slate-500">{user?.email}</div>
                </div>
              </button>

              {userMenuOpen ? (
                <MotionDiv
                  initial={{ opacity: 0, y: 8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 8, scale: 0.98 }}
                  className="absolute right-0 mt-2 w-56 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-lg"
                >
                  <div className="p-3">
                    <div className="text-xs font-semibold text-slate-500">Signed in</div>
                    <div className="mt-0.5 text-sm font-extrabold text-slate-900">{user?.fullName}</div>
                    {isAdmin ? (
                      <div className="mt-2">
                        <Badge tone="indigo">Admin access enabled</Badge>
                      </div>
                    ) : null}
                  </div>
                  <div className="border-t border-slate-200/70 p-2">
                    <div className="grid gap-2">
                      {isAdmin ? (
                        <Button variant="secondary" size="sm" onClick={() => navigate('/admin')} className="w-full justify-center gap-1.5">
                          <ShieldCheck className="h-4 w-4" />
                          Admin Portal
                        </Button>
                      ) : null}
                      <Button variant="secondary" size="sm" onClick={onLogout} className="w-full justify-center gap-1.5">
                        <LogOut className="h-4 w-4" />
                        Logout
                      </Button>
                    </div>
                  </div>
                </MotionDiv>
              ) : null}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" onClick={() => navigate('/login')}>
                Login
              </Button>
              <Button variant="primary" size="sm" onClick={() => navigate('/register')}>
                Register
              </Button>
            </div>
          )}
        </div>
      </div>

      {open ? (
        <MotionDiv
          initial={{ opacity: 0, y: -6 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -6 }}
          className="border-t border-slate-200/70 bg-white/80 backdrop-blur md:hidden"
        >
          <div className="mx-auto grid max-w-6xl gap-1 px-4 py-3 sm:px-6">
            {isAuthed ? (
              <NavLink
                to="/"
                onClick={() => setOpen(false)}
                className={clsx(
                  'rounded-xl px-3 py-2 text-sm font-extrabold hover:bg-slate-100',
                  location.pathname === '/' ? 'text-indigo-700' : 'text-slate-900'
                )}
              >
                Home
              </NavLink>
            ) : null}
            <NavLink
              to="/questions"
              onClick={() => setOpen(false)}
              className={clsx(
                'rounded-xl px-3 py-2 text-sm font-extrabold hover:bg-slate-100',
                location.pathname.startsWith('/questions') ? 'text-indigo-700' : 'text-slate-900'
              )}
            >
              Q&A
            </NavLink>
            <NavLink
              to="/lost-found"
              onClick={() => setOpen(false)}
              className={clsx(
                'rounded-xl px-3 py-2 text-sm font-extrabold hover:bg-slate-100',
                location.pathname.startsWith('/lost-found') ? 'text-indigo-700' : 'text-slate-900'
              )}
            >
              Lost & Found
            </NavLink>
            <NavLink
              to="/marketplace"
              onClick={() => setOpen(false)}
              className={clsx(
                'rounded-xl px-3 py-2 text-sm font-extrabold hover:bg-slate-100',
                location.pathname.startsWith('/marketplace') ? 'text-indigo-700' : 'text-slate-900'
              )}
            >
              Marketplace
            </NavLink>
            {isAdmin ? (
              <NavLink
                to="/admin"
                onClick={() => setOpen(false)}
                className={clsx(
                  'rounded-xl px-3 py-2 text-sm font-extrabold hover:bg-slate-100',
                  location.pathname.startsWith('/admin') ? 'text-indigo-700' : 'text-slate-900'
                )}
              >
                Admin
              </NavLink>
            ) : null}
          </div>
        </MotionDiv>
      ) : null}
    </div>
  )
}
