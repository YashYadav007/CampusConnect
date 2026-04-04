const dateFmt = new Intl.DateTimeFormat(undefined, {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
})

const dateTimeFmt = new Intl.DateTimeFormat(undefined, {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
})

export function formatDate(isoDate) {
  if (!isoDate) return ''
  // LocalDate comes as YYYY-MM-DD
  const d = new Date(`${isoDate}T00:00:00`)
  if (Number.isNaN(d.getTime())) return isoDate
  return dateFmt.format(d)
}

export function formatDateTime(isoDateTime) {
  if (!isoDateTime) return ''
  const d = new Date(isoDateTime)
  if (Number.isNaN(d.getTime())) return isoDateTime
  return dateTimeFmt.format(d)
}
