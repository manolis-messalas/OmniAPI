/**
 * Runtime API configuration for OmniAPI.
 *
 * The admin UI can talk to the backend through several "API types" that differ
 * in protocol (REST vs SOAP) and in the extra headers they send. Every call in
 * booksApi.js / authorsApi.js accepts an `apiType` string; this module is the
 * single source of truth that maps that string to concrete endpoints, headers
 * and protocol behaviour.
 *
 * Five API types are supported:
 *   - 'rest-default'    REST (Default) ...... JSON over /api/rest
 *   - 'soap'            SOAP ................. XML over /api/ws
 *   - 'rest-caching'    REST + Caching ....... REST plus client cache-hint headers
 *   - 'soap-validation' SOAP + Validation .... SOAP plus a strict-validation header
 *   - 'hybrid'          Hybrid .............. REST for reads, SOAP for writes
 *
 * SOAP note: the backend (Spring-WS) is a SOAP 1.1 service, so SOAP requests use
 * the SOAP 1.1 envelope namespace and a `text/xml` content type. The backend
 * schema uses elementFormDefault="qualified", so request children are emitted
 * with the `gs:` namespace prefix.
 */

/** Base path of the REST controllers. */
export const REST_BASE = '/api/rest'

/** Single SOAP endpoint that serves every operation. */
export const SOAP_BASE = '/api/ws'

/** Target namespace shared by every request/response element in the WSDL. */
export const SOAP_NAMESPACE = 'http://spring.io/guides/gs-producing-web-service'

/** SOAP 1.1 envelope namespace expected by Spring-WS. */
export const SOAP_ENVELOPE_NAMESPACE = 'http://schemas.xmlsoap.org/soap/envelope/'

/** Internal identifiers for the supported API types. */
export const API_TYPES = {
  REST_DEFAULT: 'rest-default',
  SOAP: 'soap',
  REST_CACHING: 'rest-caching',
  SOAP_VALIDATION: 'soap-validation',
  HYBRID: 'hybrid',
}

/** Fallback used whenever an apiType is missing or not recognised. */
export const DEFAULT_API_TYPE = API_TYPES.REST_DEFAULT

/**
 * Logical resource keys used by the API modules. Each maps to a concrete URL
 * per API type via the `endpoints` object on the config.
 */
const REST_ENDPOINTS = {
  books: `${REST_BASE}/books`,
  bookById: `${REST_BASE}/book`, // append /{name}
  createBook: `${REST_BASE}/addBook`,
  updateBook: `${REST_BASE}/books`, // append /{id}
  deleteBook: `${REST_BASE}/books`, // append /{id}
  addBookAuthor: `${REST_BASE}/addBookAuthor`,
  authors: `${REST_BASE}/authors`,
  authorById: `${REST_BASE}/author`, // append /{id}
  createAuthor: `${REST_BASE}/createAuthor`,
  updateAuthor: `${REST_BASE}/authors`, // append /{id}
  deleteAuthor: `${REST_BASE}/authors`, // append /{id}
}

/** Read-only resources. Used to split protocols for the Hybrid type. */
const READ_RESOURCES = new Set(['books', 'bookById', 'authors', 'authorById'])

/** SOAP routes every resource through the single SOAP endpoint. */
const SOAP_ENDPOINTS = Object.fromEntries(
  Object.keys(REST_ENDPOINTS).map(key => [key, SOAP_BASE])
)

/** Hybrid: REST for reads, SOAP for writes. */
const HYBRID_ENDPOINTS = Object.fromEntries(
  Object.keys(REST_ENDPOINTS).map(key => [
    key,
    READ_RESOURCES.has(key) ? REST_ENDPOINTS[key] : SOAP_BASE,
  ])
)

const REST_HEADERS = { 'Content-Type': 'application/json', Accept: 'application/json' }
const SOAP_HEADERS = { 'Content-Type': 'text/xml; charset=utf-8', Accept: 'text/xml', SOAPAction: '' }

/**
 * Build a config entry.
 *
 * @param {object} spec
 * @param {string} spec.value           internal identifier
 * @param {string} spec.name            display name
 * @param {string} spec.description     short description for the radio option
 * @param {'rest'|'soap'} spec.primaryProtocol  protocol used when no resource context is given
 * @param {object} spec.endpoints       resource -> URL map
 * @param {object} spec.extraHeaders    headers unique to this API type
 * @param {string|null} spec.soapNamespace  SOAP namespace (null for pure REST types)
 */
function buildConfig({ value, name, description, primaryProtocol, endpoints, extraHeaders, soapNamespace }) {
  const base = primaryProtocol === 'soap' ? SOAP_HEADERS : REST_HEADERS
  return {
    value,
    name,
    description,
    primaryProtocol,
    endpoints,
    extraHeaders,
    // Representative merged headers for this type (inspection / debugging).
    headers: { ...base, ...extraHeaders },
    soapNamespace,
  }
}

const CONFIGS = {
  [API_TYPES.REST_DEFAULT]: buildConfig({
    value: API_TYPES.REST_DEFAULT,
    name: 'REST (Default)',
    description: 'Standard JSON REST API over /api/rest',
    primaryProtocol: 'rest',
    endpoints: REST_ENDPOINTS,
    extraHeaders: {},
    soapNamespace: null,
  }),
  [API_TYPES.SOAP]: buildConfig({
    value: API_TYPES.SOAP,
    name: 'SOAP',
    description: 'XML SOAP web service over /api/ws',
    primaryProtocol: 'soap',
    endpoints: SOAP_ENDPOINTS,
    extraHeaders: {},
    soapNamespace: SOAP_NAMESPACE,
  }),
  [API_TYPES.REST_CACHING]: buildConfig({
    value: API_TYPES.REST_CACHING,
    name: 'REST + Caching',
    description: 'REST with client cache-hint headers',
    primaryProtocol: 'rest',
    endpoints: REST_ENDPOINTS,
    extraHeaders: { 'Cache-Control': 'max-age=60', 'X-Api-Variant': 'rest-caching' },
    soapNamespace: null,
  }),
  [API_TYPES.SOAP_VALIDATION]: buildConfig({
    value: API_TYPES.SOAP_VALIDATION,
    name: 'SOAP + Validation',
    description: 'SOAP with a strict-validation header',
    primaryProtocol: 'soap',
    endpoints: SOAP_ENDPOINTS,
    extraHeaders: { 'X-Validation': 'strict' },
    soapNamespace: SOAP_NAMESPACE,
  }),
  [API_TYPES.HYBRID]: buildConfig({
    value: API_TYPES.HYBRID,
    name: 'Hybrid',
    description: 'REST for reads, SOAP for writes',
    primaryProtocol: 'rest',
    endpoints: HYBRID_ENDPOINTS,
    extraHeaders: { 'X-Api-Variant': 'hybrid' },
    soapNamespace: SOAP_NAMESPACE,
  }),
}

/**
 * Options for rendering a radio-button group of API types.
 * @type {Array<{value:string,name:string,label:string,description:string}>}
 */
export const API_TYPE_OPTIONS = Object.values(CONFIGS).map(c => ({
  value: c.value,
  name: c.name,
  label: c.name,
  description: c.description,
}))

/**
 * Resolve the config for an API type, falling back to the default for unknown
 * or missing values.
 * @param {string} [apiType]
 * @returns {object} the config object
 */
export function getApiConfig(apiType) {
  return CONFIGS[apiType] || CONFIGS[DEFAULT_API_TYPE]
}

/**
 * Resolve the URL for a logical resource under a given API type.
 * @param {string} apiType
 * @param {string} resource  one of the keys in REST_ENDPOINTS
 * @returns {string} the endpoint URL (full path from the site root)
 */
export function getApiEndpoint(apiType, resource) {
  const config = getApiConfig(apiType)
  return config.endpoints[resource] ?? REST_ENDPOINTS[resource]
}

/** @returns {boolean} true when the endpoint is the SOAP endpoint. */
export function isSoapEndpoint(endpoint) {
  return endpoint === SOAP_BASE
}

/**
 * Resolve the headers to send for an API type. When a `resource` is provided,
 * the protocol is decided per-resource (this matters for the Hybrid type, where
 * reads are REST and writes are SOAP); otherwise the type's primary protocol is
 * used.
 * @param {string} apiType
 * @param {string} [resource]
 * @returns {Record<string,string>}
 */
export function getApiHeaders(apiType, resource) {
  const config = getApiConfig(apiType)
  const soap = resource
    ? isSoapEndpoint(config.endpoints[resource] ?? '')
    : config.primaryProtocol === 'soap'
  const base = soap ? SOAP_HEADERS : REST_HEADERS
  return { ...base, ...config.extraHeaders }
}

// ---------------------------------------------------------------------------
// SOAP helpers (pure — no network). Shared by booksApi.js and authorsApi.js.
// ---------------------------------------------------------------------------

/**
 * Escape a value for safe inclusion as XML text content.
 * @param {*} value
 * @returns {string}
 */
export function escapeXml(value) {
  if (value === null || value === undefined) return ''
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')
}

/**
 * Wrap an inner request fragment in a SOAP 1.1 envelope. The `gs` prefix is
 * bound to the backend's target namespace so callers can write `<gs:...>`.
 * @param {string} innerXml  the request element(s) for the SOAP Body
 * @returns {string} a complete SOAP envelope
 */
export function buildSoapEnvelope(innerXml) {
  return (
    '<?xml version="1.0" encoding="UTF-8"?>' +
    `<soapenv:Envelope xmlns:soapenv="${SOAP_ENVELOPE_NAMESPACE}" xmlns:gs="${SOAP_NAMESPACE}">` +
    '<soapenv:Header/>' +
    `<soapenv:Body>${innerXml}</soapenv:Body>` +
    '</soapenv:Envelope>'
  )
}

/**
 * Build an Error carrying a `response.data.message` shape so that UI components
 * (which read `err.response?.data?.message`) display SOAP fault text the same
 * way they display REST error bodies.
 * @param {string} message
 * @returns {Error}
 */
export function makeApiError(message) {
  const err = new Error(message)
  err.response = { data: { message } }
  return err
}

/**
 * Read the text content of the first descendant with the given local name,
 * regardless of namespace.
 * @param {Element|Document|null|undefined} parent
 * @param {string} localName
 * @returns {string|undefined}
 */
export function getElementText(parent, localName) {
  if (!parent) return undefined
  const el = parent.getElementsByTagNameNS('*', localName)[0]
  return el ? el.textContent : undefined
}

/**
 * Parse a string into a number, or undefined when not parseable.
 * @param {string|undefined} value
 * @returns {number|undefined}
 */
export function toNumber(value) {
  if (value === undefined || value === null || value === '') return undefined
  const n = Number(value)
  return Number.isNaN(n) ? undefined : n
}

/**
 * Parse a raw SOAP response body into an XML Document, throwing a meaningful
 * error on SOAP faults or malformed XML. Degrades gracefully if DOMParser is
 * not available in the host environment.
 * @param {string} xmlText  the raw response body
 * @returns {Document}
 */
export function parseSoapResponse(xmlText) {
  if (typeof DOMParser === 'undefined') {
    throw makeApiError('Cannot parse SOAP response: DOMParser is not available in this environment')
  }
  let doc
  try {
    doc = new DOMParser().parseFromString(xmlText ?? '', 'text/xml')
  } catch {
    throw makeApiError('Failed to parse SOAP response XML')
  }
  // Browsers report malformed XML via a <parsererror> element rather than throwing.
  if (doc.getElementsByTagName('parsererror').length > 0) {
    throw makeApiError('Malformed SOAP response XML')
  }
  const fault = extractSoapFault(doc)
  if (fault) {
    throw makeApiError(fault)
  }
  return doc
}

/**
 * Extract a human-readable fault message from a SOAP response, supporting both
 * SOAP 1.1 (`<faultstring>`) and SOAP 1.2 (`<Reason><Text>`) shapes.
 * @param {Document} doc
 * @returns {string|null} the fault text, or null when there is no fault
 */
function extractSoapFault(doc) {
  const faults = doc.getElementsByTagNameNS('*', 'Fault')
  if (faults.length === 0) return null
  const fault = faults[0]
  const message =
    getElementText(fault, 'faultstring') || // SOAP 1.1
    getElementText(fault, 'Text') ||         // SOAP 1.2 Reason/Text
    getElementText(fault, 'faultcode') ||
    'SOAP request failed'
  return message
}
