import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from './Button'

export function Pagination({ page, hasNext, onPrev, onNext, className }) {
  return (
    <div className={className}>
      <div className="flex items-center justify-between gap-3">
        <Button variant="secondary" size="sm" onClick={onPrev} disabled={page <= 0} className="gap-1.5">
          <ChevronLeft className="h-4 w-4" /> Prev
        </Button>
        <div className="text-sm font-semibold text-slate-600">Page {page + 1}</div>
        <Button variant="secondary" size="sm" onClick={onNext} disabled={!hasNext} className="gap-1.5">
          Next <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}

