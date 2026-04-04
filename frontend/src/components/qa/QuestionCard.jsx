import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { MessageSquareText } from 'lucide-react'
import { TagBadge } from './TagBadge'
import { formatDateTime } from '../../utils/formatDate'

export function QuestionCard({ q }) {
  const MotionDiv = motion.div
  const answered = (q.answerCount || 0) > 0

  return (
    <MotionDiv whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 300, damping: 24 }} className="cc-card cc-card-hover p-5">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <Link to={`/questions/${q.id}`} className="block">
            <div className="text-base font-extrabold tracking-tight text-slate-900 hover:text-indigo-700">
              {q.title}
            </div>
            {q.description ? (
              <div className="mt-2 line-clamp-2 text-sm text-slate-600">{q.description}</div>
            ) : (
              <div className="mt-2 text-sm text-slate-500">No description provided.</div>
            )}
          </Link>

          {q.tags?.length ? (
            <div className="mt-3 flex flex-wrap gap-2">
              {q.tags.map((t) => (
                <TagBadge key={t} tag={t} />
              ))}
            </div>
          ) : null}

          <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs font-semibold text-slate-500">
            <span>By {q.user?.fullName || 'Unknown'}</span>
            <span className="h-1 w-1 rounded-full bg-slate-300" />
            <span>{formatDateTime(q.createdAt)}</span>
            <span className="h-1 w-1 rounded-full bg-slate-300" />
            <span className={answered ? 'text-emerald-700' : 'text-slate-500'}>{answered ? 'Answered' : 'No answers yet'}</span>
          </div>
        </div>

        <div className="shrink-0 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-center">
          <div className="flex items-center justify-center gap-1 text-sm font-extrabold text-slate-900">
            <MessageSquareText className="h-4 w-4 text-indigo-600" />
            <span>{q.answerCount ?? 0}</span>
          </div>
          <div className="mt-0.5 text-[11px] font-semibold text-slate-500">answers</div>
        </div>
      </div>
    </MotionDiv>
  )
}
