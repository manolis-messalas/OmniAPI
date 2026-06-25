import { useState } from 'react'
import { createAuthor, updateAuthor } from '../../api/authorsApi'

export default function AuthorForm({ initial, onSaved, onCancel }) {
  const [form, setForm] = useState({
    authorName: initial?.authorName ?? '',
    dateOfBirth: initial?.dateOfBirth ?? '',
    countryOfOrigin: initial?.countryOfOrigin ?? '',
  })
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  // Generated once per form open so retries of the same submit reuse the same key
  const [idempotencyKey] = useState(() => crypto.randomUUID())

  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      if (initial) {
        await updateAuthor(initial.authorId, form)
      } else {
        await createAuthor(form, idempotencyKey)
      }
      onSaved()
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Name</label>
        <input
          name="authorName"
          value={form.authorName}
          onChange={handleChange}
          required
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Date of Birth</label>
        <input
          name="dateOfBirth"
          value={form.dateOfBirth}
          onChange={handleChange}
          required
          placeholder="e.g. 1990-01-15"
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Country</label>
        <input
          name="countryOfOrigin"
          value={form.countryOfOrigin}
          onChange={handleChange}
          required
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      {error && <p className="sm:col-span-3 text-red-600 text-sm">{error}</p>}
      <div className="sm:col-span-3 flex space-x-2">
        <button
          type="submit"
          disabled={saving}
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
        >
          {saving ? 'Saving...' : initial ? 'Update' : 'Create'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="border border-gray-300 text-gray-700 px-4 py-2 rounded text-sm font-medium hover:bg-gray-50"
        >
          Cancel
        </button>
      </div>
    </form>
  )
}
