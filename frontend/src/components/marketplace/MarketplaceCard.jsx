import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Badge } from '../common/Badge'
import { formatDateTime } from '../../utils/formatDate'
import { toneForMarketplaceStatus } from './marketplaceBadges'

export function MarketplaceCard({ item }) {
  const MotionDiv = motion.div

  return (
    <MotionDiv whileHover={{ y: -4 }} transition={{ type: 'spring', stiffness: 320, damping: 24 }} className="cc-card cc-card-hover overflow-hidden">
      <Link to={`/marketplace/${item.id}`} className="block">
        {item.imageUrl ? (
          <div className="relative h-48 overflow-hidden bg-slate-100">
            <img src={item.imageUrl} alt={item.title} className="h-full w-full object-cover" loading="lazy" />
            <div className="absolute left-4 top-4 flex items-center gap-2">
              <Badge tone={toneForMarketplaceStatus(item.status)}>{item.status}</Badge>
            </div>
          </div>
        ) : (
          <div className="relative flex h-48 items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
            <div className="absolute left-4 top-4">
              <Badge tone={toneForMarketplaceStatus(item.status)}>{item.status}</Badge>
            </div>
            <div className="text-sm font-bold text-slate-600">No image provided</div>
          </div>
        )}

        <div className="p-5">
          <div className="flex items-start justify-between gap-3">
            <div>
              <div className="text-base font-extrabold tracking-tight text-slate-900">{item.title}</div>
              <div className="mt-1 text-sm font-semibold text-slate-500">{item.category} · {item.conditionLabel}</div>
            </div>
            <div className="text-right">
              <div className="text-lg font-extrabold text-slate-900">₹{item.price}</div>
              <div className="text-xs font-semibold text-indigo-700">Token ₹{item.tokenAmount}</div>
            </div>
          </div>

          {item.description ? (
            <div className="mt-3 line-clamp-2 text-sm text-slate-600">{item.description}</div>
          ) : (
            <div className="mt-3 text-sm text-slate-500">No description provided.</div>
          )}

          <div className="mt-4 flex items-center justify-between gap-3 text-xs font-semibold text-slate-500">
            <span>Seller: {item.seller?.fullName || 'Unknown'}</span>
            <span>{formatDateTime(item.createdAt)}</span>
          </div>
        </div>
      </Link>
    </MotionDiv>
  )
}
