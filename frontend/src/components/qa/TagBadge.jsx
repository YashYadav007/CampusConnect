import { Badge } from '../common/Badge'

export function TagBadge({ tag }) {
  return (
    <Badge tone="indigo" className="whitespace-nowrap">
      {tag}
    </Badge>
  )
}
