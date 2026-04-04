import { forwardRef } from 'react'
import clsx from 'clsx'
import { motion } from 'framer-motion'

const styles = {
  primary:
    'bg-gradient-to-b from-indigo-600 to-blue-600 text-white shadow-sm hover:shadow-md focus-visible:ring-indigo-500',
  secondary: 'bg-white text-slate-900 border border-slate-200 hover:bg-slate-50 focus-visible:ring-slate-300',
  ghost: 'bg-transparent text-slate-700 hover:bg-slate-100 focus-visible:ring-slate-300',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-500',
}

export const Button = forwardRef(function Button(
  { variant = 'primary', size = 'md', loading, className, children, ...props },
  ref
) {
  const MotionButton = motion.button
  const sizeCls =
    size === 'sm'
      ? 'h-9 px-3 text-sm'
      : size === 'lg'
        ? 'h-12 px-5 text-base'
        : 'h-10 px-4 text-sm'

  return (
    <MotionButton
      ref={ref}
      whileTap={{ scale: 0.98 }}
      whileHover={{ y: -1 }}
      disabled={loading || props.disabled}
      className={clsx(
        'inline-flex items-center justify-center gap-2 rounded-xl font-semibold outline-none transition',
        'focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-50',
        'disabled:cursor-not-allowed disabled:opacity-60',
        sizeCls,
        styles[variant],
        className
      )}
      {...props}
    >
      {loading ? (
        <span className="inline-flex items-center gap-2">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          <span>Loading</span>
        </span>
      ) : (
        children
      )}
    </MotionButton>
  )
})
