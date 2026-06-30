/**
 * Books API client with runtime protocol switching.
 *
 * Every public function accepts an optional `apiType` (default 'rest-default')
 * that selects the protocol and headers via apiConfig.js. REST calls use axios
 * verbs; SOAP calls build a SOAP 1.1 envelope and parse the XML response back
 * into the same JSON shape the REST endpoints return, so UI components are
 * agnostic to the transport.
 *
 * Returned book shape (both protocols):
 *   { id, version?, bookName, publicationYear, authorDTO: { authorName } }
 */
import client from './client'
import {
  DEFAULT_API_TYPE,
  SOAP_NAMESPACE,
  getApiEndpoint,
  getApiHeaders,
  isSoapEndpoint,
  buildSoapEnvelope,
  parseSoapResponse,
  getElementText,
  escapeXml,
  toNumber,
  makeApiError,
} from './apiConfig'

// ---------------------------------------------------------------------------
// Transport helpers
// ---------------------------------------------------------------------------

/**
 * Issue a REST request through the shared axios client. `baseURL` is cleared so
 * the full path from apiConfig is used as-is (the client's auth + 401 handling
 * interceptors still apply).
 */
function restRequest(method, url, { data, headers } = {}) {
  return client.request({ method, url, data, headers, baseURL: '' }).then(r => r.data)
}

/**
 * POST a SOAP envelope and return the parsed XML Document (throws on SOAP fault).
 */
async function soapRequest(url, innerXml, headers) {
  const response = await client.request({
    method: 'POST',
    url,
    data: buildSoapEnvelope(innerXml),
    headers,
    baseURL: '',
    responseType: 'text',
  })
  return parseSoapResponse(response.data)
}

// ---------------------------------------------------------------------------
// SOAP request builders (XSD: Book = id, version, name, publicationYear, authorName)
// ---------------------------------------------------------------------------

function buildBookFields({ id, version, bookName, publicationYear, authorName }) {
  return (
    (id !== undefined && id !== null ? `<gs:id>${escapeXml(id)}</gs:id>` : '') +
    (version !== undefined && version !== null ? `<gs:version>${escapeXml(version)}</gs:version>` : '') +
    `<gs:name>${escapeXml(bookName)}</gs:name>` +
    `<gs:publicationYear>${escapeXml(publicationYear)}</gs:publicationYear>` +
    `<gs:authorName>${escapeXml(authorName)}</gs:authorName>`
  )
}

// ---------------------------------------------------------------------------
// SOAP response parsers (map <Book> XML -> REST-shaped JSON)
// ---------------------------------------------------------------------------

/** Map a single SOAP <Book> element to the REST-shaped book object. */
function mapBookElement(el) {
  if (!el) return null
  return {
    id: toNumber(getElementText(el, 'id')),
    version: toNumber(getElementText(el, 'version')),
    bookName: getElementText(el, 'name'),
    publicationYear: getElementText(el, 'publicationYear'),
    authorDTO: { authorName: getElementText(el, 'authorName') },
  }
}

/** Map a GetBooksResponse document to an array of book objects. */
function parseBookList(doc) {
  const nodes = doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'books')
  return Array.from(nodes).map(mapBookElement)
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Fetch all books.
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<Array>} list of books
 */
export async function fetchBooks(apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'books')
  const headers = getApiHeaders(apiType, 'books')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(endpoint, '<gs:GetBooksRequest/>', headers)
    return parseBookList(doc)
  }
  return restRequest('GET', endpoint, { headers })
}

/**
 * Fetch a single book. On this backend books are looked up by name, so `bookId`
 * is used as the lookup key for both protocols.
 * @param {string|number} bookId the book name/identifier to look up
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<object|null>}
 */
export async function fetchBookById(bookId, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'bookById')
  const headers = getApiHeaders(apiType, 'bookById')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(
      endpoint,
      `<gs:GetBookRequest><gs:name>${escapeXml(bookId)}</gs:name></gs:GetBookRequest>`,
      headers
    )
    return mapBookElement(doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'book')[0])
  }
  return restRequest('GET', `${endpoint}/${encodeURIComponent(bookId)}`, { headers })
}

/**
 * Create a book.
 * @param {object} bookData { bookName, publicationYear, authorDTO: { authorName } }
 * @param {string} idempotencyKey de-duplication key (sent as header for REST,
 *   as <idempotencyKey> for SOAP)
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function createBook(bookData, idempotencyKey, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'createBook')
  const headers = getApiHeaders(apiType, 'createBook')
  if (isSoapEndpoint(endpoint)) {
    const inner =
      '<gs:CreateBookRequest>' +
      `<gs:idempotencyKey>${escapeXml(idempotencyKey)}</gs:idempotencyKey>` +
      '<gs:book>' +
      buildBookFields({
        bookName: bookData.bookName,
        publicationYear: bookData.publicationYear,
        authorName: bookData.authorDTO?.authorName,
      }) +
      '</gs:book>' +
      '</gs:CreateBookRequest>'
    const doc = await soapRequest(endpoint, inner, headers)
    return {
      bookId: toNumber(getElementText(doc.documentElement, 'bookId')),
      message: getElementText(doc.documentElement, 'message'),
    }
  }
  return restRequest('POST', endpoint, {
    data: bookData,
    headers: { ...headers, 'Idempotency-Key': idempotencyKey },
  })
}

/**
 * Create a book together with its author in one call.
 * @param {object} data { bookName, publicationYear, authorName, dateOfBirth, countryOfOrigin }
 * @param {string} idempotencyKey de-duplication key
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function createBookAuthor(data, idempotencyKey, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'addBookAuthor')
  const headers = getApiHeaders(apiType, 'addBookAuthor')
  if (isSoapEndpoint(endpoint)) {
    const inner =
      '<gs:CreateBookAuthorRequest>' +
      `<gs:idempotencyKey>${escapeXml(idempotencyKey)}</gs:idempotencyKey>` +
      '<gs:bookAuthorDTO>' +
      `<gs:bookName>${escapeXml(data.bookName)}</gs:bookName>` +
      `<gs:dateOfBirth>${escapeXml(data.dateOfBirth)}</gs:dateOfBirth>` +
      `<gs:countryOfOrigin>${escapeXml(data.countryOfOrigin)}</gs:countryOfOrigin>` +
      `<gs:authorName>${escapeXml(data.authorName)}</gs:authorName>` +
      `<gs:publicationYear>${escapeXml(data.publicationYear)}</gs:publicationYear>` +
      '</gs:bookAuthorDTO>' +
      '</gs:CreateBookAuthorRequest>'
    const doc = await soapRequest(endpoint, inner, headers)
    const success = getElementText(doc.documentElement, 'success')
    if (success !== 'true') {
      throw makeApiError('SOAP create book+author reported failure')
    }
    return { success: true }
  }
  return restRequest('POST', endpoint, {
    data,
    headers: { ...headers, 'Idempotency-Key': idempotencyKey },
  })
}

/**
 * Update a book.
 * @param {string|number} bookId id of the book to update
 * @param {object} bookData { bookName, publicationYear, authorDTO: { authorName }, version? }
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function updateBook(bookId, bookData, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'updateBook')
  const headers = getApiHeaders(apiType, 'updateBook')
  if (isSoapEndpoint(endpoint)) {
    const inner =
      '<gs:UpdateBookRequest><gs:book>' +
      buildBookFields({
        id: bookId,
        version: bookData.version,
        bookName: bookData.bookName,
        publicationYear: bookData.publicationYear,
        authorName: bookData.authorDTO?.authorName,
      }) +
      '</gs:book></gs:UpdateBookRequest>'
    const doc = await soapRequest(endpoint, inner, headers)
    return mapBookElement(doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'book')[0])
  }
  return restRequest('PUT', `${endpoint}/${encodeURIComponent(bookId)}`, { data: bookData, headers })
}

/**
 * Delete a book.
 * @param {string|number} bookId id of the book to delete
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function deleteBook(bookId, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'deleteBook')
  const headers = getApiHeaders(apiType, 'deleteBook')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(
      endpoint,
      `<gs:DeleteBookRequest><gs:bookId>${escapeXml(bookId)}</gs:bookId></gs:DeleteBookRequest>`,
      headers
    )
    if (getElementText(doc.documentElement, 'status') !== 'true') {
      throw makeApiError(`SOAP delete failed for book ${bookId}`)
    }
    return true
  }
  return restRequest('DELETE', `${endpoint}/${encodeURIComponent(bookId)}`, { headers })
}

// ---------------------------------------------------------------------------
// Backward-compatible aliases (default REST behaviour preserved)
// ---------------------------------------------------------------------------

export const getBooks = fetchBooks
export const getBook = fetchBookById
export const addBook = createBook
export const addBookAuthor = createBookAuthor
