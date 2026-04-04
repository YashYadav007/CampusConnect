import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useForm } from 'react-hook-form'
import { Mail, LockKeyhole } from 'lucide-react'
import { Button } from '../components/common/Button'
import { Input } from '../components/common/Input'
import { Alert } from '../components/common/Alert'
import { getApiErrorMessage } from '../api/axios'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'

export function LoginPage() {
  const { isAuthed, login } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const MotionDiv = motion.div
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const from = location.state?.from || '/'

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({ defaultValues: { email: '', password: '' }, mode: 'onChange' })

  useEffect(() => {
    if (isAuthed) navigate('/', { replace: true })
  }, [isAuthed, navigate])

  const onSubmit = async (values) => {
    setErr('')
    setLoading(true)
    try {
      await login(values)
      showToast({ tone: 'success', title: 'Logged in', message: 'Welcome back.' })
      navigate(from, { replace: true })
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  const loginDemo = async (email) => {
    setErr('')
    setLoading(true)
    try {
      await login({ email, password: 'password123' })
      showToast({ tone: 'success', title: 'Logged in (demo)', message: `Signed in as ${email}` })
      navigate('/', { replace: true })
    } catch (e) {
      const m = getApiErrorMessage(e)
      setErr(m)
      showToast({ tone: 'error', title: 'Demo login failed', message: m })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="cc-gradient">
      <div className="mx-auto grid min-h-[calc(100vh-72px)] max-w-6xl items-center px-4 py-10 sm:px-6">
        <div className="grid items-center gap-8 lg:grid-cols-2">
          <MotionDiv initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
            <div className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-white/70 px-3 py-2 text-xs font-extrabold text-slate-700">
              <span className="h-2 w-2 rounded-full bg-indigo-600" />
              JWT-secured Student Portal
            </div>
            <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Welcome back to <span className="bg-gradient-to-r from-indigo-700 to-blue-600 bg-clip-text text-transparent">CampusConnect</span>
            </h1>
            <p className="mt-4 max-w-xl text-sm leading-relaxed text-slate-600">
              Ask questions, share answers, and track lost or found items across campus. Log in to continue.
            </p>
          </MotionDiv>

          <MotionDiv initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.05 }}>
            <div className="cc-card p-6 sm:p-8">
              <div className="text-2xl font-extrabold tracking-tight text-slate-900">Login</div>
              <div className="mt-1 text-sm font-semibold text-slate-600">Use your campus email to sign in.</div>

              {err ? <Alert tone="error" title="Login failed" className="mt-4">{err}</Alert> : null}

              {import.meta.env.DEV ? (
                <div className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                  <div className="text-sm font-extrabold text-slate-900">Demo accounts</div>
                  <div className="mt-1 text-sm text-slate-600">
                    Password: <span className="font-extrabold">password123</span>
                  </div>
                  <div className="mt-3 grid gap-2 sm:grid-cols-3">
                    <Button type="button" variant="secondary" size="sm" onClick={() => loginDemo('aarav@demo.com')} disabled={loading}>
                      Owner
                    </Button>
                    <Button type="button" variant="secondary" size="sm" onClick={() => loginDemo('diya@demo.com')} disabled={loading}>
                      Helper
                    </Button>
                    <Button type="button" variant="secondary" size="sm" onClick={() => loginDemo('kabir@demo.com')} disabled={loading}>
                      Voter/Claimer
                    </Button>
                  </div>
                  <div className="mt-2">
                    <Button type="button" variant="secondary" size="sm" onClick={() => loginDemo('admin@campusconnect.com')} disabled={loading} className="w-full">
                      Admin
                    </Button>
                  </div>
                </div>
              ) : null}

              <form className="mt-6 grid gap-4" onSubmit={handleSubmit(onSubmit)}>
                <Input
                  label="Email"
                  placeholder="you@college.edu"
                  type="email"
                  autoComplete="email"
                  error={errors.email?.message}
                  {...register('email', { required: 'Email is required' })}
                />
                <Input
                  label="Password"
                  placeholder="••••••••"
                  type="password"
                  autoComplete="current-password"
                  error={errors.password?.message}
                  {...register('password', { required: 'Password is required' })}
                />

                <Button type="submit" loading={loading} disabled={!isValid || loading} className="mt-1 w-full">
                  <Mail className="h-4 w-4" />
                  Sign in
                </Button>

                <div className="mt-2 flex items-center justify-between text-sm">
                  <div className="inline-flex items-center gap-2 text-slate-500">
                    <LockKeyhole className="h-4 w-4" />
                    <span className="font-semibold">Secure JWT session</span>
                  </div>
                  <Link to="/register" className="font-extrabold text-indigo-700 hover:text-indigo-800">
                    Create account
                  </Link>
                </div>
              </form>
            </div>
          </MotionDiv>
        </div>
      </div>
    </div>
  )
}
