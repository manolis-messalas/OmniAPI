import client from './client'

export const getBooks = () => client.get('/books').then(r => r.data)
export const addBook = (data, idempotencyKey) =>
  client.post('/addBook', data, { headers: { 'Idempotency-Key': idempotencyKey } })
export const addBookAuthor = (data, idempotencyKey) =>
  client.post('/addBookAuthor', data, { headers: { 'Idempotency-Key': idempotencyKey } })
export const updateBook = (id, data) => client.put(`/books/${id}`, data)
export const deleteBook = (id) => client.delete(`/books/${id}`)
