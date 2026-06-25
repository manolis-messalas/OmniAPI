import client from './client'

export const getAuthors = () => client.get('/authors').then(r => r.data)
export const getAuthor = (id) => client.get(`/author/${id}`).then(r => r.data)
export const createAuthor = (data, idempotencyKey) =>
  client.post('/createAuthor', data, { headers: { 'Idempotency-Key': idempotencyKey } })
export const updateAuthor = (id, data) => client.put(`/authors/${id}`, data)
export const deleteAuthor = (id) => client.delete(`/authors/${id}`)
