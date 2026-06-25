import { useState } from 'react'
import { addBook, updateBook } from '../../api/booksApi'

export default function BookForm({ initial, authors, onSaved, onCancel }) {
  const [form, setForm] = useState({
    bookName: initial?.bookName ?? '',
    publicationYear: initial?.publicationYear ?? '',
    authorName: initial?.authorDTO?.authorName ?? '',
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
    const payload = {
      bookName: form.bookName,
      publicationYear: form.publicationYear,
      authorDTO: { authorName: form.authorName },
    }
    try {
      if (initial) {
        await updateBook(initial.id, payload)
      } else {
        await addBook(payload, idempotencyKey)
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
        <label className="block text-xs font-medium text-gray-600 mb-1">Title</label>
        <input
          name="bookName"
          value={form.bookName}
          onChange={handleChange}
          required
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Publication Year</label>
        <input
          name="publicationYear"
          value={form.publicationYear}
          onChange={handleChange}
          required
          placeholder="e.g. 2023"
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Author</label>
        <select
          name="authorName"
          value={form.authorName}
          onChange={handleChange}
          required
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
        >
          <option value="">Select author...</option>
          {authors.map(a => (
            <option key={a.authorId} value={a.authorName}>{a.authorName}</option>
          ))}
        </select>
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
