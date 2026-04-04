import { Inbox } from 'lucide-react'

export function EmptyState({ title = 'Nothing here yet', hint = 'Try adjusting filters or adding something new.' }) {
  return (
    <div className="cc-card p-8 text-center">
      <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100">
        <Inbox className="h-6 w-6 text-slate-600" />
      </div>
      <div className="mt-4 text-lg font-bold text-slate-900">{title}</div>
      <div className="mt-1 text-sm text-slate-600">{hint}</div>
    </div>
  )
}
