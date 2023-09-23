package com.mixtape.spotifyauthserver.federation;

import com.mixtape.spotifyauthserver.MockOAuth2AuthorizationHelper;
import com.mixtape.spotifyauthserver.data.authclient.ProviderTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FederatedIdentityIdTokenCustomizerTest {

    public static final String MOCK_PROVIDER_TOKEN_VALUE = "provider-token";
    public static final String PROVIDER_TOKEN_CLAIM = "provider_token";
    @Mock
    ProviderTokenService mockProviderTokenService;

    FederatedIdentityIdTokenCustomizer customizer;

    @BeforeEach
    void setUp() {
        customizer = new FederatedIdentityIdTokenCustomizer(mockProviderTokenService);
    }

    @Test
    void customize_appendsProviderToken_whenAvailable() {
        JwtEncodingContext context = JwtEncodingContext
                .with(JwsHeader.with(SignatureAlgorithm.RS256), JwtClaimsSet.builder())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorization(MockOAuth2AuthorizationHelper.MOCK_OAUTH2_AUTHORIZATION)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        when(mockProviderTokenService.getProviderTokenForUser(MockOAuth2AuthorizationHelper.MOCK_OAUTH2_AUTHORIZATION))
                .thenReturn(Optional.of(MOCK_PROVIDER_TOKEN_VALUE));

        customizer.customize(context);

        JwtClaimsSet claimsSet = context.getClaims().build();
        assertThat(claimsSet.hasClaim(PROVIDER_TOKEN_CLAIM)).isTrue();
        assertThat(claimsSet.getClaimAsString("provider_token")).isEqualTo(MOCK_PROVIDER_TOKEN_VALUE);
    }

    @Test
    void customize_doesNotIncludeProviderToken_whenDoesntExist() {
        JwtEncodingContext context = JwtEncodingContext
                .with(
                        JwsHeader.with(SignatureAlgorithm.RS256),
                        JwtClaimsSet.builder()
                            .claim("dummy", "dummy_claim")
                )
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorization(MockOAuth2AuthorizationHelper.MOCK_OAUTH2_AUTHORIZATION)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        when(mockProviderTokenService.getProviderTokenForUser(MockOAuth2AuthorizationHelper.MOCK_OAUTH2_AUTHORIZATION))
                .thenReturn(Optional.empty());

        customizer.customize(context);

        JwtClaimsSet claimsSet = context.getClaims().build();
        assertThat(claimsSet.hasClaim(PROVIDER_TOKEN_CLAIM)).isFalse();
    }

    @Test
    void customize_doesNotIncludeProviderToken_whenAuthorizationIsNull() {
        JwtEncodingContext context = JwtEncodingContext
                .with(
                        JwsHeader.with(SignatureAlgorithm.RS256),
                        JwtClaimsSet.builder()
                                .claim("dummy", "dummy_claim")
                )
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // .authorization(MockOAuth2AuthorizationHelper.MOCK_OAUTH2_AUTHORIZATION)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        customizer.customize(context);

        JwtClaimsSet claimsSet = context.getClaims().build();
        assertThat(claimsSet.hasClaim(PROVIDER_TOKEN_CLAIM)).isFalse();
    }
}