import { useState, useEffect } from 'react'
import { getAuthors, deleteAuthor } from '../../api/authorsApi'
import AuthorForm from './AuthorForm'

export default function AuthorList() {
  const [authors, setAuthors] = useState([])
  const [editing, setEditing] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      setError('')
      const data = await getAuthors()
      setAuthors(data)
    } catch {
      setError('Failed to load authors')
    }
  }

  useEffect(() => { load() }, [])

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this author?')) return
    try {
      await deleteAuthor(id)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  const handleSaved = () => {
    setEditing(null)
    setShowCreate(false)
    load()
  }

  const openCreate = () => {
    setEditing(null)
    setShowCreate(true)
  }

  const openEdit = (author) => {
    setShowCreate(false)
    setEditing(author)
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="px-4 py-3 border-b flex justify-between items-center">
        <h2 className="font-semibold text-gray-800">Authors</h2>
        <button
          onClick={openCreate}
          className="bg-blue-600 text-white px-3 py-1.5 rounded text-sm font-medium hover:bg-blue-700"
        >
          + New Author
        </button>
      </div>

      {(showCreate || editing) && (
        <div className="px-4 py-4 border-b bg-gray-50">
          <AuthorForm
            initial={editing}
            onSaved={handleSaved}
            onCancel={() => { setEditing(null); setShowCreate(false) }}
          />
        </div>
      )}

      {error && <p className="px-4 py-3 text-red-600 text-sm">{error}</p>}

      <table className="w-full text-sm">
        <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
          <tr>
            <th className="px-4 py-3 text-left">ID</th>
            <th className="px-4 py-3 text-left">Name</th>
            <th className="px-4 py-3 text-left">Date of Birth</th>
            <th className="px-4 py-3 text-left">Country</th>
            <th className="px-4 py-3 text-left">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {authors.map(a => (
            <tr key={a.authorId} className="hover:bg-gray-50">
              <td className="px-4 py-3 text-gray-400">{a.authorId}</td>
              <td className="px-4 py-3 font-medium text-gray-800">{a.authorName}</td>
              <td className="px-4 py-3 text-gray-600">{a.dateOfBirth}</td>
              <td className="px-4 py-3 text-gray-600">{a.countryOfOrigin}</td>
              <td className="px-4 py-3 space-x-3">
                <button
                  onClick={() => openEdit(a)}
                  className="text-blue-600 hover:underline text-sm"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(a.authorId)}
                  className="text-red-600 hover:underline text-sm"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {authors.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                No authors found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
