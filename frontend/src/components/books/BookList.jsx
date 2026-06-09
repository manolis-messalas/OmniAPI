import { useState, useEffect } from 'react'
import { getBooks, deleteBook } from '../../api/booksApi'
import { getAuthors } from '../../api/authorsApi'
import BookForm from './BookForm'

export default function BookList() {
  const [books, setBooks] = useState([])
  const [authors, setAuthors] = useState([])
  const [editing, setEditing] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      setError('')
      const [booksData, authorsData] = await Promise.all([getBooks(), getAuthors()])
      setBooks(booksData)
      setAuthors(authorsData)
    } catch {
      setError('Failed to load books')
    }
  }

  useEffect(() => { load() }, [])

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this book?')) return
    try {
      await deleteBook(id)
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

  const openEdit = (book) => {
    setShowCreate(false)
    setEditing(book)
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="px-4 py-3 border-b flex justify-between items-center">
        <h2 className="font-semibold text-gray-800">Books</h2>
        <button
          onClick={openCreate}
          className="bg-blue-600 text-white px-3 py-1.5 rounded text-sm font-medium hover:bg-blue-700"
        >
          + New Book
        </button>
      </div>

      {(showCreate || editing) && (
        <div className="px-4 py-4 border-b bg-gray-50">
          <BookForm
            initial={editing}
            authors={authors}
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
            <th className="px-4 py-3 text-left">Title</th>
            <th className="px-4 py-3 text-left">Year</th>
            <th className="px-4 py-3 text-left">Author</th>
            <th className="px-4 py-3 text-left">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {books.map(b => (
            <tr key={b.id} className="hover:bg-gray-50">
              <td className="px-4 py-3 text-gray-400">{b.id}</td>
              <td className="px-4 py-3 font-medium text-gray-800">{b.bookName}</td>
              <td className="px-4 py-3 text-gray-600">{b.publicationYear}</td>
              <td className="px-4 py-3 text-gray-600">{b.authorDTO?.authorName}</td>
              <td className="px-4 py-3 space-x-3">
                <button
                  onClick={() => openEdit(b)}
                  className="text-blue-600 hover:underline text-sm"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(b.id)}
                  className="text-red-600 hover:underline text-sm"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {books.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                No books found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
