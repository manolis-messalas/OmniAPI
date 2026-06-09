import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AuthorList from '../components/authors/AuthorList'
import BookList from '../components/books/BookList'

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('authors')
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow-sm">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-800">OmniAPI Admin</h1>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-600 hover:text-gray-900"
          >
            Sign Out
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6">
        <div className="flex space-x-1 mb-6">
          {['authors', 'books'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 rounded font-medium text-sm capitalize ${
                activeTab === tab
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border border-gray-200'
              }`}
            >
              {tab}
            </button>
          ))}
        </div>

        {activeTab === 'authors' && <AuthorList />}
        {activeTab === 'books' && <BookList />}
      </main>
    </div>
  )
}
