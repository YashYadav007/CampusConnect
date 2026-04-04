import { motion } from 'framer-motion'
import { ArrowBigDown, ArrowBigUp, CheckCircle2 } from 'lucide-react'
import { Button } from '../common/Button'
import { Badge } from '../common/Badge'
import { formatDateTime } from '../../utils/formatDate'

export function AnswerCard({
  answer,
  canVote,
  onVote,
  canAccept,
  onAccept,
  userVote,
}) {
  const MotionDiv = motion.div
  const isAccepted = Boolean(answer.isAccepted)
  const upActive = userVote === 'UPVOTE'
  const downActive = userVote === 'DOWNVOTE'

  return (
    <MotionDiv
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className={
        isAccepted
          ? 'cc-card p-5 ring-2 ring-emerald-200/70'
          : 'cc-card cc-card-hover p-5'
      }
    >
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="whitespace-pre-wrap text-sm leading-relaxed text-slate-800">{answer.content}</div>

          <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs font-semibold text-slate-500">
            <span>By {answer.user?.fullName || 'Unknown'}</span>
            <span className="h-1 w-1 rounded-full bg-slate-300" />
            <span>{formatDateTime(answer.createdAt)}</span>
            {isAccepted ? (
              <>
                <span className="h-1 w-1 rounded-full bg-slate-300" />
                <span className="inline-flex items-center gap-1 font-extrabold text-emerald-700">
                  <CheckCircle2 className="h-4 w-4" /> Accepted Answer
                </span>
              </>
            ) : null}
          </div>
        </div>

        <div className="shrink-0 text-right">
          <div className="flex items-center justify-end gap-2">
            <Badge tone={answer.score > 0 ? 'green' : answer.score < 0 ? 'red' : 'slate'}>
              Score {answer.score ?? 0}
            </Badge>
            {isAccepted ? <Badge tone="green">Accepted</Badge> : null}
          </div>

          <div className="mt-3 flex items-center justify-end gap-2">
            <div className="grid grid-cols-2 gap-2">
              <Button
                variant="secondary"
                size="sm"
                className={upActive ? 'gap-1.5 border-indigo-300 bg-indigo-50' : 'gap-1.5'}
                disabled={!canVote}
                onClick={() => onVote?.('UPVOTE')}
              >
                <ArrowBigUp className="h-4 w-4" />
                <span className="tabular-nums">{answer.upvoteCount ?? 0}</span>
              </Button>
              <Button
                variant="secondary"
                size="sm"
                className={downActive ? 'gap-1.5 border-red-300 bg-red-50' : 'gap-1.5'}
                disabled={!canVote}
                onClick={() => onVote?.('DOWNVOTE')}
              >
                <ArrowBigDown className="h-4 w-4" />
                <span className="tabular-nums">{answer.downvoteCount ?? 0}</span>
              </Button>
            </div>
          </div>

          {canAccept && !isAccepted ? (
            <div className="mt-3">
              <Button variant="primary" size="sm" className="gap-1.5" onClick={onAccept}>
                <CheckCircle2 className="h-4 w-4" /> Accept
              </Button>
            </div>
          ) : null}
        </div>
      </div>
    </MotionDiv>
  )
}
