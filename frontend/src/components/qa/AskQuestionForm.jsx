import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '../common/Button'
import { Input } from '../common/Input'
import { Textarea } from '../common/Textarea'

export function AskQuestionForm({ onSubmit, loading }) {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: { title: '', description: '', tagsText: '' },
    mode: 'onChange',
  })

  const hints = useMemo(
    () => [
      'Keep titles specific: "JWT token not validating in Spring Security"',
      'Add tags as comma-separated keywords: java, spring, jwt',
    ],
    []
  )

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="cc-card p-6">
      <div className="grid gap-4">
        <Input
          label="Title"
          placeholder="Write a clear, specific question title"
          error={errors.title?.message}
          {...register('title', {
            required: 'Title is required',
            minLength: { value: 5, message: 'Title must be at least 5 characters' },
            maxLength: { value: 200, message: 'Title must be at most 200 characters' },
          })}
        />

        <Textarea
          label="Description"
          placeholder="Add context, code snippets, and expected behavior"
          error={errors.description?.message}
          rows={7}
          {...register('description', {
            maxLength: { value: 20000, message: 'Description is too long' },
          })}
        />

        <Input
          label="Tags"
          placeholder="java, spring, jwt"
          error={errors.tagsText?.message}
          {...register('tagsText', {
            maxLength: { value: 200, message: 'Too many tags' },
          })}
        />

        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
          <div className="text-sm font-bold text-slate-900">Tips</div>
          <ul className="mt-2 grid gap-1 text-sm text-slate-600">
            {hints.map((h) => (
              <li key={h}>- {h}</li>
            ))}
          </ul>
        </div>

        <div className="flex justify-end">
          <Button type="submit" loading={loading} disabled={!isValid || loading}>
            Post question
          </Button>
        </div>
      </div>
    </form>
  )
}
