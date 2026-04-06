export function toneForMarketplaceStatus(status) {
  if (status === 'AVAILABLE') return 'green'
  if (status === 'RESERVED') return 'amber'
  if (status === 'SOLD') return 'slate'
  return 'slate'
}
