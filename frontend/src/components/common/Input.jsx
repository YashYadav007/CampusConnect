import { forwardRef } from 'react'
import clsx from 'clsx'

export const Input = forwardRef(function Input({ label, error, className, ...props }, ref) {
  return (
    <label className="block">
      {label ? <div className="mb-1.5 text-sm font-semibold text-slate-700">{label}</div> : null}
      <input
        ref={ref}
        className={clsx(
          'w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-slate-900',
          'shadow-sm outline-none transition',
          'placeholder:text-slate-400',
          'focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100',
          error ? 'border-red-300 focus:border-red-300 focus:ring-red-100' : 'border-slate-200',
          className
        )}
        {...props}
      />
      {error ? <div className="mt-1.5 text-sm text-red-600">{error}</div> : null}
    </label>
  )
})
