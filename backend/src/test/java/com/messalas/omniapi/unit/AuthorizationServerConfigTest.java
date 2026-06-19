package com.messalas.omniapi.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class AuthorizationServerConfigTest {

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private AuthorizationServerSettings authorizationServerSettings;

    @Test
    public void testSpaClientIsRegisteredAsPublicPkceClient() {
        RegisteredClient client = registeredClientRepository.findByClientId("omniapi-spa");

        assertNotNull(client);
        assertEquals(ClientAuthenticationMethod.NONE, client.getClientAuthenticationMethods().iterator().next());
        assertTrue(client.getClientSettings().isRequireProofKey());
        assertFalse(client.getClientSettings().isRequireAuthorizationConsent());
        assertTrue(client.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE));
        assertTrue(client.getRedirectUris().contains("http://localhost:5173/oauth/callback"));
        assertTrue(client.getScopes().contains("api.read"));
        assertTrue(client.getScopes().contains("api.write"));
    }

    @Test
    public void testJwtDecoderBeanIsConfigured() {
        assertNotNull(jwtDecoder);
    }

    @Test
    public void testAuthorizationServerIssuerMatchesConfiguredProperty() {
        assertEquals("http://localhost:9090", authorizationServerSettings.getIssuer());
    }
}
