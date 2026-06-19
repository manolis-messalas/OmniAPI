package com.messalas.omniapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the full self-hosted Authorization Code + PKCE flow end-to-end:
 * unauthenticated /oauth2/authorize -> redirect to /login -> resource-owner
 * login -> redirect back to /oauth2/authorize -> authorization code issued ->
 * code exchanged at /oauth2/token -> resulting JWT used as a Bearer token
 * against the resource-server-protected REST API.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
public class OAuth2AuthorizationCodeFlowIT {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationCodeFlowIT.class);

    private static final String CLIENT_ID = "omniapi-spa";
    private static final String REDIRECT_URI = "http://localhost:5173/oauth/callback";
    private static final String SCOPE = "openid api.read api.write";

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();

    @Test
    public void testFullAuthorizationCodeFlowIssuesWorkingBearerToken() throws Exception {
        String baseUrl = "http://localhost:" + port;
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = "test-state-" + System.currentTimeMillis();

        logger.info("Starting testFullAuthorizationCodeFlowIssuesWorkingBearerToken");

        String authorizeUrl = baseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=" + CLIENT_ID
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&scope=" + encode(SCOPE)
                + "&state=" + state
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";

        HttpResponse<String> authorizeResponse = get(authorizeUrl);
        assertEquals(302, authorizeResponse.statusCode(), "unauthenticated authorize request should redirect to login");
        String loginUrl = resolveLocation(baseUrl, authorizeResponse);

        HttpResponse<String> loginPageResponse = get(loginUrl);
        assertEquals(200, loginPageResponse.statusCode());

        HttpResponse<String> loginPostResponse = postForm(baseUrl + "/login", "username=admin&password=admin");
        assertEquals(302, loginPostResponse.statusCode(), "login should succeed and redirect back to the saved request");
        String savedAuthorizeUrl = resolveLocation(baseUrl, loginPostResponse);

        HttpResponse<String> authorizeAgainResponse = get(savedAuthorizeUrl);
        assertEquals(302, authorizeAgainResponse.statusCode(), "authenticated authorize request should issue a code via redirect");
        String redirectWithCode = resolveLocation(baseUrl, authorizeAgainResponse);

        Map<String, String> callbackParams = parseQuery(URI.create(redirectWithCode).getQuery());
        assertEquals(state, callbackParams.get("state"));
        String code = callbackParams.get("code");
        assertNotNull(code, "authorization code missing from callback redirect: " + redirectWithCode);

        String tokenBody = "grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&client_id=" + CLIENT_ID
                + "&code_verifier=" + encode(codeVerifier);

        HttpResponse<String> tokenResponse = postForm(baseUrl + "/oauth2/token", tokenBody);
        assertEquals(200, tokenResponse.statusCode(), "token exchange failed: " + tokenResponse.body());

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenJson = new ObjectMapper().readValue(tokenResponse.body(), Map.class);
        String accessToken = (String) tokenJson.get("access_token");
        assertNotNull(accessToken);
        assertNotNull(tokenJson.get("expires_in"));
        // No refresh_token is expected here: Spring Authorization Server's
        // OAuth2RefreshTokenGenerator deliberately refuses to issue one to public
        // (ClientAuthenticationMethod.NONE) clients on the authorization_code grant.
        assertNull(tokenJson.get("refresh_token"));

        HttpResponse<String> apiResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/api/rest/authors"))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, apiResponse.statusCode(), "valid bearer token should be accepted by the resource server");

        // Use a fresh, cookie-less client here: the shared httpClient still carries the
        // JSESSIONID established during the /login step above, and a valid Spring Security
        // session is also accepted on this chain (see SecurityConfig's formLogin() note) -
        // that's expected chain-overlap behavior, not what this assertion is checking.
        HttpClient noSessionClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> unauthenticatedApiResponse = noSessionClient.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/api/rest/authors")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(401, unauthenticatedApiResponse.statusCode(), "request without a bearer token or session must be rejected");

        logger.info("testFullAuthorizationCodeFlowIssuesWorkingBearerToken completed successfully");
    }

    private HttpResponse<String> get(String url) throws Exception {
        return httpClient.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(String url, String body) throws Exception {
        return httpClient.send(
                HttpRequest.newBuilder(URI.create(url))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String resolveLocation(String baseUrl, HttpResponse<String> response) {
        String location = response.headers().firstValue("Location")
                .orElseThrow(() -> new AssertionError("missing Location header on redirect"));
        return location.startsWith("http") ? location : baseUrl + location;
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(java.util.stream.Collectors.toMap(
                        pair -> decode(pair[0]),
                        pair -> pair.length > 1 ? decode(pair[1]) : ""));
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return base64UrlEncode(bytes);
    }

    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(hash);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
