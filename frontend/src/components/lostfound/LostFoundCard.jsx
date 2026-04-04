import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { MapPin, CalendarDays, Image as ImageIcon } from 'lucide-react'
import { Badge } from '../common/Badge'
import { formatDate, formatDateTime } from '../../utils/formatDate'
import { toneForItemStatus, toneForPostType } from './lostFoundBadges'

export function LostFoundCard({ post }) {
  const MotionDiv = motion.div
  return (
    <MotionDiv whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 300, damping: 24 }} className="cc-card cc-card-hover overflow-hidden">
      <Link to={`/lost-found/${post.id}`} className="block">
        {post.imageUrl ? (
          <div className="relative h-40 w-full overflow-hidden bg-slate-100">
            <img src={post.imageUrl} alt={post.title} className="h-full w-full object-cover" loading="lazy" />
            <div className="absolute left-4 top-4 flex items-center gap-2">
              <Badge tone={toneForPostType(post.type)}>{post.type}</Badge>
              <Badge tone={toneForItemStatus(post.status)}>{post.status}</Badge>
            </div>
          </div>
        ) : (
          <div className="relative flex h-40 items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
            <div className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white/70 px-3 py-2 text-sm font-bold text-slate-700">
              <ImageIcon className="h-4 w-4 text-indigo-600" /> No image
            </div>
            <div className="absolute left-4 top-4 flex items-center gap-2">
              <Badge tone={toneForPostType(post.type)}>{post.type}</Badge>
              <Badge tone={toneForItemStatus(post.status)}>{post.status}</Badge>
            </div>
          </div>
        )}

        <div className="p-5">
          <div className="text-base font-extrabold tracking-tight text-slate-900">{post.title}</div>
          {post.description ? (
            <div className="mt-2 line-clamp-2 text-sm text-slate-600">{post.description}</div>
          ) : (
            <div className="mt-2 text-sm text-slate-500">No description provided.</div>
          )}

          <div className="mt-4 grid gap-2 text-xs font-semibold text-slate-600">
            <div className="flex items-center gap-2">
              <MapPin className="h-4 w-4 text-indigo-600" />
              <span>{post.location}</span>
            </div>
            <div className="flex items-center gap-2">
              <CalendarDays className="h-4 w-4 text-indigo-600" />
              <span>Incident: {formatDate(post.dateOfIncident)}</span>
            </div>
            <div className="text-[11px] font-semibold text-slate-500">
              Posted by {post.user?.fullName || 'Unknown'} · {formatDateTime(post.createdAt)}
            </div>
          </div>
        </div>
      </Link>
    </MotionDiv>
  )
}
