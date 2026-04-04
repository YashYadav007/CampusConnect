import clsx from 'clsx'
import { AlertCircle, CheckCircle2, Info } from 'lucide-react'

const tones = {
  error: 'border-red-200 bg-red-50 text-red-800',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  info: 'border-slate-200 bg-white text-slate-800',
}

export function Alert({ tone = 'info', title, children, className }) {
  const Icon = tone === 'error' ? AlertCircle : tone === 'success' ? CheckCircle2 : Info
  return (
    <div className={clsx('cc-card rounded-2xl border p-4', tones[tone], className)}>
      <div className="flex items-start gap-3">
        <div className="mt-0.5 rounded-xl bg-white/60 p-2">
          <Icon className="h-5 w-5" />
        </div>
        <div className="min-w-0">
          {title ? <div className="text-sm font-extrabold">{title}</div> : null}
          {children ? <div className="mt-0.5 text-sm">{children}</div> : null}
        </div>
      </div>
    </div>
  )
}

