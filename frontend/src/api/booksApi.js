import client from './client'

export const getBooks = () => client.get('/books').then(r => r.data)
export const addBook = (data) => client.post('/addBook', data)
export const addBookAuthor = (data) => client.post('/addBookAuthor', data)
export const updateBook = (id, data) => client.put(`/books/${id}`, data)
export const deleteBook = (id) => client.delete(`/books/${id}`)
