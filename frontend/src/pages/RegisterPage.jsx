import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useForm } from 'react-hook-form'
import { GraduationCap, UserPlus } from 'lucide-react'
import { Button } from '../components/common/Button'
import { Input } from '../components/common/Input'
import { Alert } from '../components/common/Alert'
import { getApiErrorMessage } from '../api/axios'
import { registerUser } from '../api/authApi'
import { useAuth } from '../hooks/useAuth'

export function RegisterPage() {
  const { isAuthed, login } = useAuth()
  const navigate = useNavigate()
  const MotionDiv = motion.div
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
    watch,
  } = useForm({
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      course: '',
      yearOfStudy: 1,
    },
    mode: 'onChange',
  })

  const year = watch('yearOfStudy')

  useEffect(() => {
    if (isAuthed) navigate('/', { replace: true })
  }, [isAuthed, navigate])

  const onSubmit = async (values) => {
    setErr('')
    setLoading(true)
    try {
      await registerUser({
        ...values,
        fullName: values.fullName.trim(),
        email: values.email.trim(),
        course: values.course.trim(),
        yearOfStudy: Number(values.yearOfStudy),
      })
      // auto-login for MVP polish
      await login({ email: values.email.trim(), password: values.password })
      navigate('/', { replace: true })
    } catch (e) {
      setErr(getApiErrorMessage(e))
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
              <GraduationCap className="h-4 w-4 text-indigo-700" />
              Build your campus presence
            </div>
            <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Create your <span className="bg-gradient-to-r from-indigo-700 to-blue-600 bg-clip-text text-transparent">CampusConnect</span> account
            </h1>
            <p className="mt-4 max-w-xl text-sm leading-relaxed text-slate-600">
              Your profile helps classmates trust your answers and makes lost items easier to return.
            </p>

            <div className="mt-6 grid gap-3 text-sm text-slate-700">
              <div className="cc-card p-4">
                <div className="font-extrabold">Reputation matters</div>
                <div className="mt-1 text-slate-600">Upvotes and accepted answers boost your credibility.</div>
              </div>
              <div className="cc-card p-4">
                <div className="font-extrabold">Lost & Found updates</div>
                <div className="mt-1 text-slate-600">Report items with a location and incident date for clarity.</div>
              </div>
            </div>
          </MotionDiv>

          <MotionDiv initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.05 }}>
            <div className="cc-card p-6 sm:p-8">
              <div className="text-2xl font-extrabold tracking-tight text-slate-900">Register</div>
              <div className="mt-1 text-sm font-semibold text-slate-600">It takes less than a minute.</div>

              {err ? <Alert tone="error" title="Registration failed" className="mt-4">{err}</Alert> : null}

              <form className="mt-6 grid gap-4" onSubmit={handleSubmit(onSubmit)}>
                <Input
                  label="Full name"
                  placeholder="Your name"
                  error={errors.fullName?.message}
                  {...register('fullName', {
                    required: 'Full name is required',
                    minLength: { value: 2, message: 'Name is too short' },
                    maxLength: { value: 100, message: 'Name is too long' },
                  })}
                />
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
                  placeholder="Create a password"
                  type="password"
                  autoComplete="new-password"
                  error={errors.password?.message}
                  {...register('password', {
                    required: 'Password is required',
                    minLength: { value: 6, message: 'Password must be at least 6 characters' },
                    maxLength: { value: 200, message: 'Password is too long' },
                  })}
                />

                <div className="grid gap-4 sm:grid-cols-2">
                  <Input
                    label="Course"
                    placeholder="B.Tech CSE"
                    error={errors.course?.message}
                    {...register('course', {
                      required: 'Course is required',
                      minLength: { value: 2, message: 'Course is too short' },
                      maxLength: { value: 100, message: 'Course is too long' },
                    })}
                  />
                  <Input
                    label="Year of study"
                    type="number"
                    min={1}
                    max={10}
                    error={errors.yearOfStudy?.message}
                    {...register('yearOfStudy', {
                      required: 'Year is required',
                      min: { value: 1, message: 'Year must be at least 1' },
                      max: { value: 10, message: 'Year must be at most 10' },
                    })}
                  />
                </div>

                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
                  <div className="font-extrabold text-slate-900">Preview</div>
                  <div className="mt-1 text-slate-600">
                    Year {year || 1} student. You can update details later.
                  </div>
                </div>

                <Button type="submit" loading={loading} disabled={!isValid || loading} className="mt-1 w-full">
                  <UserPlus className="h-4 w-4" />
                  Create account
                </Button>

                <div className="mt-2 text-center text-sm text-slate-600">
                  Already have an account?{' '}
                  <Link to="/login" className="font-extrabold text-indigo-700 hover:text-indigo-800">
                    Login
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
