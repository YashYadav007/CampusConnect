import { useCallback, useMemo, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react'

import { ToastContext } from './toastContext'

function toneStyles(tone) {
  if (tone === 'success') return 'border-emerald-200 bg-emerald-50 text-emerald-900'
  if (tone === 'error') return 'border-red-200 bg-red-50 text-red-900'
  return 'border-slate-200 bg-white text-slate-900'
}

function ToneIcon({ tone }) {
  const cls = tone === 'success' ? 'text-emerald-700' : tone === 'error' ? 'text-red-700' : 'text-slate-700'
  const Icon = tone === 'success' ? CheckCircle2 : tone === 'error' ? AlertCircle : Info
  return <Icon className={`h-5 w-5 ${cls}`} />
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((t) => t.filter((x) => x.id !== id))
  }, [])

  const showToast = useCallback(
    ({ tone = 'info', title, message, durationMs = 3500 }) => {
      const id = `${Date.now()}_${Math.random().toString(16).slice(2)}`
      const toast = { id, tone, title, message }
      setToasts((t) => [toast, ...t].slice(0, 5))

      if (durationMs > 0) {
        window.setTimeout(() => dismiss(id), durationMs)
      }
      return id
    },
    [dismiss]
  )

  const value = useMemo(() => ({ showToast, dismiss }), [showToast, dismiss])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed right-4 top-4 z-[200] w-[min(420px,calc(100vw-2rem))] space-y-2">
        <AnimatePresence>
          {toasts.map((t) => (
            <motion.div
              key={t.id}
              initial={{ opacity: 0, x: 18, scale: 0.98 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, x: 18, scale: 0.98 }}
              transition={{ type: 'spring', stiffness: 320, damping: 28 }}
              className={`pointer-events-auto cc-card border p-4 shadow-md ${toneStyles(t.tone)}`}
            >
              <div className="flex items-start gap-3">
                <div className="mt-0.5 rounded-xl bg-white/60 p-2">
                  <ToneIcon tone={t.tone} />
                </div>
                <div className="min-w-0 flex-1">
                  {t.title ? <div className="text-sm font-extrabold">{t.title}</div> : null}
                  {t.message ? <div className="mt-0.5 text-sm text-slate-700">{t.message}</div> : null}
                </div>
                <button
                  type="button"
                  onClick={() => dismiss(t.id)}
                  className="rounded-xl p-2 text-slate-500 hover:bg-white/70 hover:text-slate-800"
                  aria-label="Dismiss"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  )
}
