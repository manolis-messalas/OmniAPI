/**
 * Authors API client with runtime protocol switching.
 *
 * Mirrors booksApi.js: every public function accepts an optional `apiType`
 * (default 'rest-default') that selects the protocol and headers via
 * apiConfig.js. REST calls use axios verbs; SOAP calls build a SOAP 1.1
 * envelope and parse the XML response back into the same JSON shape the REST
 * endpoints return, so UI components are agnostic to the transport.
 *
 * Returned author shape (both protocols):
 *   { authorId, version?, authorName, dateOfBirth, countryOfOrigin }
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
// SOAP request builders (XSD: Author = id, version, name, dateOfBirth, countryOfOrigin)
// ---------------------------------------------------------------------------

function buildAuthorFields({ id, version, authorName, dateOfBirth, countryOfOrigin }) {
  return (
    (id !== undefined && id !== null ? `<gs:id>${escapeXml(id)}</gs:id>` : '') +
    (version !== undefined && version !== null ? `<gs:version>${escapeXml(version)}</gs:version>` : '') +
    `<gs:name>${escapeXml(authorName)}</gs:name>` +
    `<gs:dateOfBirth>${escapeXml(dateOfBirth)}</gs:dateOfBirth>` +
    `<gs:countryOfOrigin>${escapeXml(countryOfOrigin)}</gs:countryOfOrigin>`
  )
}

// ---------------------------------------------------------------------------
// SOAP response parsers (map <Author> XML -> REST-shaped JSON)
// ---------------------------------------------------------------------------

/** Map a single SOAP <Author> element to the REST-shaped author object. */
function mapAuthorElement(el) {
  if (!el) return null
  return {
    authorId: toNumber(getElementText(el, 'id')),
    version: toNumber(getElementText(el, 'version')),
    authorName: getElementText(el, 'name'),
    dateOfBirth: getElementText(el, 'dateOfBirth'),
    countryOfOrigin: getElementText(el, 'countryOfOrigin'),
  }
}

/** Map a GetAuthorsResponse document to an array of author objects. */
function parseAuthorList(doc) {
  const nodes = doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'authors')
  return Array.from(nodes).map(mapAuthorElement)
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Fetch all authors.
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<Array>} list of authors
 */
export async function fetchAuthors(apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'authors')
  const headers = getApiHeaders(apiType, 'authors')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(endpoint, '<gs:GetAuthorsRequest/>', headers)
    return parseAuthorList(doc)
  }
  return restRequest('GET', endpoint, { headers })
}

/**
 * Fetch a single author by id.
 * @param {string|number} authorId
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<object|null>}
 */
export async function fetchAuthorById(authorId, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'authorById')
  const headers = getApiHeaders(apiType, 'authorById')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(
      endpoint,
      `<gs:GetAuthorRequest><gs:id>${escapeXml(authorId)}</gs:id></gs:GetAuthorRequest>`,
      headers
    )
    return mapAuthorElement(doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'author')[0])
  }
  return restRequest('GET', `${endpoint}/${encodeURIComponent(authorId)}`, { headers })
}

/**
 * Create an author.
 * @param {object} authorData { authorName, dateOfBirth, countryOfOrigin }
 * @param {string} idempotencyKey de-duplication key (sent as header for REST,
 *   as <idempotencyKey> for SOAP)
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function createAuthor(authorData, idempotencyKey, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'createAuthor')
  const headers = getApiHeaders(apiType, 'createAuthor')
  if (isSoapEndpoint(endpoint)) {
    const inner =
      '<gs:CreateAuthorRequest>' +
      `<gs:idempotencyKey>${escapeXml(idempotencyKey)}</gs:idempotencyKey>` +
      '<gs:author>' +
      buildAuthorFields({
        authorName: authorData.authorName,
        dateOfBirth: authorData.dateOfBirth,
        countryOfOrigin: authorData.countryOfOrigin,
      }) +
      '</gs:author>' +
      '</gs:CreateAuthorRequest>'
    const doc = await soapRequest(endpoint, inner, headers)
    return {
      authorId: toNumber(getElementText(doc.documentElement, 'authorId')),
      message: getElementText(doc.documentElement, 'message'),
    }
  }
  return restRequest('POST', endpoint, {
    data: authorData,
    headers: { ...headers, 'Idempotency-Key': idempotencyKey },
  })
}

/**
 * Update an author.
 * @param {string|number} authorId id of the author to update
 * @param {object} authorData { authorName, dateOfBirth, countryOfOrigin, version? }
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function updateAuthor(authorId, authorData, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'updateAuthor')
  const headers = getApiHeaders(apiType, 'updateAuthor')
  if (isSoapEndpoint(endpoint)) {
    const inner =
      '<gs:UpdateAuthorRequest><gs:author>' +
      buildAuthorFields({
        id: authorId,
        version: authorData.version,
        authorName: authorData.authorName,
        dateOfBirth: authorData.dateOfBirth,
        countryOfOrigin: authorData.countryOfOrigin,
      }) +
      '</gs:author></gs:UpdateAuthorRequest>'
    const doc = await soapRequest(endpoint, inner, headers)
    return mapAuthorElement(doc.getElementsByTagNameNS(SOAP_NAMESPACE, 'author')[0])
  }
  return restRequest('PUT', `${endpoint}/${encodeURIComponent(authorId)}`, { data: authorData, headers })
}

/**
 * Delete an author.
 * @param {string|number} authorId id of the author to delete
 * @param {string} [apiType] selects protocol/headers (default 'rest-default')
 * @returns {Promise<*>}
 */
export async function deleteAuthor(authorId, apiType = DEFAULT_API_TYPE) {
  const endpoint = getApiEndpoint(apiType, 'deleteAuthor')
  const headers = getApiHeaders(apiType, 'deleteAuthor')
  if (isSoapEndpoint(endpoint)) {
    const doc = await soapRequest(
      endpoint,
      `<gs:DeleteAuthorRequest><gs:authorId>${escapeXml(authorId)}</gs:authorId></gs:DeleteAuthorRequest>`,
      headers
    )
    if (getElementText(doc.documentElement, 'status') !== 'true') {
      throw makeApiError(`SOAP delete failed for author ${authorId}`)
    }
    return true
  }
  return restRequest('DELETE', `${endpoint}/${encodeURIComponent(authorId)}`, { headers })
}

// ---------------------------------------------------------------------------
// Backward-compatible aliases (default REST behaviour preserved)
// ---------------------------------------------------------------------------

export const getAuthors = fetchAuthors
export const getAuthor = fetchAuthorById
