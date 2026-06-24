package com.messalas.omniapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared PKCE flow helper for integration tests that need a valid JWT bearer
 * token before exercising a protected endpoint. Subclasses call
 * acquireAccessToken(baseUrl) in @BeforeAll (requires @TestInstance(PER_CLASS)).
 */
abstract class OAuth2TestSupport {

    protected static final String CLIENT_ID = "omniapi-spa";
    protected static final String REDIRECT_URI = "http://localhost:5173/oauth/callback";
    protected static final String SCOPE = "openid api.read api.write";

    protected String acquireAccessToken(String baseUrl) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = "test-state-" + System.currentTimeMillis();

        String authorizeUrl = baseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + encode(CLIENT_ID)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&scope=" + encode(SCOPE)
                + "&state=" + state
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";

        // unauthenticated /oauth2/authorize → redirect to /login
        HttpResponse<String> r1 = get(client, authorizeUrl);
        String loginUrl = resolveLocation(baseUrl, r1);

        // GET /login page (establishes session)
        get(client, loginUrl);

        // POST credentials → redirect back to saved authorize URL
        HttpResponse<String> r3 = postForm(client, baseUrl + "/login", "username=admin&password=admin");
        String savedAuthorizeUrl = resolveLocation(baseUrl, r3);

        // authenticated /oauth2/authorize → redirect with authorization code
        HttpResponse<String> r4 = get(client, savedAuthorizeUrl);
        String redirectWithCode = resolveLocation(baseUrl, r4);

        String code = parseQuery(URI.create(redirectWithCode).getQuery()).get("code");

        String tokenBody = "grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&client_id=" + CLIENT_ID
                + "&code_verifier=" + encode(codeVerifier);

        HttpResponse<String> tokenResponse = postForm(client, baseUrl + "/oauth2/token", tokenBody);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenJson = new ObjectMapper().readValue(tokenResponse.body(), Map.class);
        String accessToken = (String) tokenJson.get("access_token");
        if (accessToken == null) {
            throw new IllegalStateException("Token acquisition failed: " + tokenResponse.body());
        }
        return accessToken;
    }

    private HttpResponse<String> get(HttpClient client, String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(HttpClient client, String url, String body) throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create(url))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String resolveLocation(String baseUrl, HttpResponse<String> response) {
        String location = response.headers().firstValue("Location")
                .orElseThrow(() -> new AssertionError("missing Location header: " + response.statusCode()));
        return location.startsWith("http") ? location : baseUrl + location;
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> decode(pair[0]),
                        pair -> pair.length > 1 ? decode(pair[1]) : ""));
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    protected String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
