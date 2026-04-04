import { useForm } from 'react-hook-form'
import { Button } from '../common/Button'
import { Input } from '../common/Input'
import { Textarea } from '../common/Textarea'

export function LostFoundForm({ onSubmit, loading }) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      type: 'LOST',
      title: '',
      description: '',
      imageUrl: '',
      location: '',
      dateOfIncident: '',
    },
    mode: 'onChange',
  })

  const imageUrl = watch('imageUrl')

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="cc-card p-6">
      <div className="grid gap-4 md:grid-cols-2">
        <label className="block">
          <div className="mb-1.5 text-sm font-semibold text-slate-700">Type</div>
          <select
            className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-900 shadow-sm outline-none transition focus:border-indigo-300 focus:ring-4 focus:ring-indigo-100"
            {...register('type', { required: 'Type is required' })}
          >
            <option value="LOST">LOST</option>
            <option value="FOUND">FOUND</option>
          </select>
          {errors.type?.message ? <div className="mt-1.5 text-sm text-red-600">{errors.type.message}</div> : null}
        </label>

        <Input
          label="Location"
          placeholder="Central Library"
          error={errors.location?.message}
          {...register('location', {
            required: 'Location is required',
            minLength: { value: 2, message: 'Location must be at least 2 characters' },
            maxLength: { value: 255, message: 'Location must be at most 255 characters' },
          })}
        />

        <div className="md:col-span-2">
          <Input
            label="Title"
            placeholder="Black wallet near library"
            error={errors.title?.message}
            {...register('title', {
              required: 'Title is required',
              minLength: { value: 5, message: 'Title must be at least 5 characters' },
              maxLength: { value: 200, message: 'Title must be at most 200 characters' },
            })}
          />
        </div>

        <div className="md:col-span-2">
          <Textarea
            label="Description"
            placeholder="Any identifying details"
            error={errors.description?.message}
            rows={6}
            {...register('description', { maxLength: { value: 20000, message: 'Description is too long' } })}
          />
        </div>

        <Input
          label="Image URL (optional)"
          placeholder="https://..."
          error={errors.imageUrl?.message}
          {...register('imageUrl', { maxLength: { value: 2048, message: 'URL is too long' } })}
        />

        <Input
          label="Date of Incident"
          type="date"
          error={errors.dateOfIncident?.message}
          {...register('dateOfIncident', { required: 'Date of incident is required' })}
        />

        {imageUrl ? (
          <div className="md:col-span-2">
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <div className="text-sm font-bold text-slate-900">Image Preview</div>
              <div className="mt-3 overflow-hidden rounded-2xl border border-slate-200 bg-white">
                <img
                  src={imageUrl}
                  alt="Preview"
                  className="max-h-64 w-full object-cover"
                  onError={(e) => {
                    e.currentTarget.style.display = 'none'
                  }}
                />
              </div>
            </div>
          </div>
        ) : null}

        <div className="md:col-span-2 flex justify-end">
          <Button type="submit" loading={loading} disabled={!isValid || loading}>
            Create post
          </Button>
        </div>
      </div>
    </form>
  )
}
