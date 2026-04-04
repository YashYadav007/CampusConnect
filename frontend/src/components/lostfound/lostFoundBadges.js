export function toneForPostType(type) {
  if (type === 'FOUND') return 'blue'
  if (type === 'LOST') return 'red'
  return 'slate'
}

export function toneForItemStatus(status) {
  if (status === 'OPEN') return 'slate'
  if (status === 'RESOLVED') return 'green'
  if (status === 'CLAIMED') return 'amber'
  return 'slate'
}

