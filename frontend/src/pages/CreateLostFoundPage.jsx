import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageContainer } from '../components/layout/PageContainer'
import { LostFoundForm } from '../components/lostfound/LostFoundForm'
import { Alert } from '../components/common/Alert'
import { getApiErrorMessage } from '../api/axios'
import { createLostFound } from '../api/lostFoundApi'

function normalizeOptionalText(v) {
  if (v === undefined || v === null) return null
  const t = String(v).trim()
  return t ? t : null
}

export function CreateLostFoundPage() {
  const navigate = useNavigate()
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async (values) => {
    setErr('')
    setLoading(true)
    try {
      const payload = {
        type: values.type,
        title: values.title.trim(),
        description: normalizeOptionalText(values.description),
        imageUrl: normalizeOptionalText(values.imageUrl),
        location: values.location.trim(),
        dateOfIncident: values.dateOfIncident,
      }
      const created = await createLostFound(payload)
      navigate(`/lost-found/${created.id}`)
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageContainer title="Create Lost/Found Post" subtitle="Use an image URL for now. File upload can be added later.">
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not create post">{err}</Alert> : null}
        <LostFoundForm onSubmit={onSubmit} loading={loading} />
      </div>
    </PageContainer>
  )
}

