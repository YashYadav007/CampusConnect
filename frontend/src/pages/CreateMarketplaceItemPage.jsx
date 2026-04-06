import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { PageContainer } from '../components/layout/PageContainer'
import { Input } from '../components/common/Input'
import { Textarea } from '../components/common/Textarea'
import { Button } from '../components/common/Button'
import { Alert } from '../components/common/Alert'
import { createListing } from '../api/marketplaceApi'
import { getApiErrorMessage } from '../api/axios'
import { useToast } from '../hooks/useToast'

export function CreateMarketplaceItemPage() {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting, isValid },
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      title: '',
      description: '',
      category: '',
      conditionLabel: '',
      price: '',
      tokenAmount: '',
      imageUrl: '',
    },
  })

  const price = Number(watch('price') || 0)

  const onSubmit = async (values) => {
    try {
      const payload = {
        ...values,
        description: values.description?.trim() || null,
        imageUrl: values.imageUrl?.trim() || null,
        price: Number(values.price),
        tokenAmount: Number(values.tokenAmount),
      }
      const created = await createListing(payload)
      showToast({ tone: 'success', title: 'Listing created', message: 'Your marketplace item is now live.' })
      navigate(`/marketplace/${created.id}`)
    } catch (e) {
      showToast({ tone: 'error', title: 'Could not create listing', message: getApiErrorMessage(e) })
    }
  }

  return (
    <PageContainer
      title="Create Marketplace Listing"
      subtitle="List an item with a full price and a smaller token amount buyers can pay to reserve it."
    >
      <div className="mx-auto max-w-3xl">
        <form onSubmit={handleSubmit(onSubmit)} className="cc-card grid gap-5 p-6 sm:p-7">
          <Alert tone="info" title="Token reservation flow">
            Buyers will pay only the token amount online. Your item moves to RESERVED only after backend payment verification succeeds.
          </Alert>

          <Input
            label="Title"
            placeholder="e.g. HP Scientific Calculator"
            error={errors.title?.message}
            {...register('title', {
              required: 'Title is required',
              minLength: { value: 5, message: 'Title must be at least 5 characters' },
              maxLength: { value: 200, message: 'Title must be at most 200 characters' },
            })}
          />

          <Textarea
            label="Description"
            placeholder="Mention useful details, defects, accessories, and pickup notes."
            rows={5}
            error={errors.description?.message}
            {...register('description', {
              maxLength: { value: 2000, message: 'Description is too long' },
            })}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Category"
              placeholder="Electronics"
              error={errors.category?.message}
              {...register('category', {
                required: 'Category is required',
                minLength: { value: 2, message: 'Category must be at least 2 characters' },
                maxLength: { value: 50, message: 'Category must be at most 50 characters' },
              })}
            />
            <Input
              label="Condition"
              placeholder="Like New"
              error={errors.conditionLabel?.message}
              {...register('conditionLabel', {
                required: 'Condition is required',
                minLength: { value: 2, message: 'Condition must be at least 2 characters' },
                maxLength: { value: 30, message: 'Condition must be at most 30 characters' },
              })}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Full Price (INR)"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="2500"
              error={errors.price?.message}
              {...register('price', {
                required: 'Price is required',
                validate: (value) => Number(value) > 0 || 'Price must be greater than 0',
              })}
            />
            <Input
              label="Token Amount (INR)"
              type="number"
              step="0.01"
              min="0.01"
              placeholder="250"
              error={errors.tokenAmount?.message}
              {...register('tokenAmount', {
                required: 'Token amount is required',
                validate: (value) => {
                  const token = Number(value)
                  if (token <= 0) return 'Token amount must be greater than 0'
                  if (price > 0 && token > price) return 'Token amount cannot exceed full price'
                  return true
                },
              })}
            />
          </div>

          <Input
            label="Image URL"
            placeholder="https://example.com/item.jpg"
            error={errors.imageUrl?.message}
            {...register('imageUrl')}
          />

          <div className="flex flex-wrap justify-end gap-3">
            <Button type="button" variant="ghost" onClick={() => navigate('/marketplace')}>
              Cancel
            </Button>
            <Button type="submit" loading={isSubmitting} disabled={!isValid || isSubmitting}>
              Publish Listing
            </Button>
          </div>
        </form>
      </div>
    </PageContainer>
  )
}
