import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PageContainer } from '../components/layout/PageContainer'
import { AskQuestionForm } from '../components/qa/AskQuestionForm'
import { Alert } from '../components/common/Alert'
import { getApiErrorMessage } from '../api/axios'
import { askQuestion } from '../api/questionApi'

function normalizeTags(tagsText) {
  if (!tagsText) return []
  const raw = tagsText
    .split(',')
    .map((t) => t.trim().toLowerCase())
    .filter(Boolean)
  return Array.from(new Set(raw)).slice(0, 10)
}

function normalizeDescription(desc) {
  if (desc === undefined || desc === null) return null
  const t = String(desc).trim()
  return t ? t : null
}

export function AskQuestionPage() {
  const navigate = useNavigate()
  const [err, setErr] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async (values) => {
    setErr('')
    setLoading(true)
    try {
      const payload = {
        title: values.title.trim(),
        description: normalizeDescription(values.description),
        tags: normalizeTags(values.tagsText),
      }
      const created = await askQuestion(payload)
      navigate(`/questions/${created.id}`)
    } catch (e) {
      setErr(getApiErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageContainer
      title="Ask a Question"
      subtitle="Write a clear title, add context, and use tags so others can find it."
    >
      <div className="grid gap-4">
        {err ? <Alert tone="error" title="Could not create question">{err}</Alert> : null}
        <AskQuestionForm onSubmit={onSubmit} loading={loading} />
      </div>
    </PageContainer>
  )
}

