import { createPortal } from 'react-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import { Button } from './Button'

export function Modal({ open, title, subtitle, onClose, children, footer }) {
  if (typeof document === 'undefined') return null

  return createPortal(
    <AnimatePresence>
      {open ? (
        <motion.div
          className="fixed inset-0 z-[100] flex items-end justify-center p-4 sm:items-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          role="dialog"
          aria-modal="true"
        >
          <motion.button
            className="absolute inset-0 bg-slate-900/40"
            onClick={onClose}
            aria-label="Close modal"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          />

          <motion.div
            className="relative w-full max-w-xl"
            initial={{ opacity: 0, y: 18, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 18, scale: 0.98 }}
            transition={{ type: 'spring', stiffness: 280, damping: 24 }}
          >
            <div className="cc-card overflow-hidden">
              <div className="flex items-start justify-between gap-3 border-b border-slate-200/70 bg-white/70 p-5 backdrop-blur">
                <div className="min-w-0">
                  <div className="text-lg font-extrabold tracking-tight text-slate-900">{title}</div>
                  {subtitle ? <div className="mt-1 text-sm font-semibold text-slate-600">{subtitle}</div> : null}
                </div>
                <Button variant="ghost" size="sm" onClick={onClose} aria-label="Close" className="shrink-0">
                  <X className="h-5 w-5" />
                </Button>
              </div>

              <div className="p-5">{children}</div>

              {footer ? (
                <div className="border-t border-slate-200/70 bg-slate-50 p-4">
                  <div className="flex items-center justify-end gap-2">{footer}</div>
                </div>
              ) : null}
            </div>
          </motion.div>
        </motion.div>
      ) : null}
    </AnimatePresence>,
    document.body
  )
}

